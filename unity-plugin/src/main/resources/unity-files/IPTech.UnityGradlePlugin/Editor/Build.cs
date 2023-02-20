using System;
using System.Linq;
using UnityEditor;
using UnityEditor.Android;
using UnityEditor.Build.Reporting;
using System.IO;
using UnityEditor.Build;
using UnityEngine;
using System.Text;

namespace IPTech.UnityGradlePlugin {
	public class Build {
		int buildNumber;
		BuildPlayerOptions buildPlayerOptions;
		string bundleIdentifier;
		string productName;

		public Build(string outputPath, bool developmentBuild, int buildNumber, bool usesNonExemptEncryption = false, string bundleIdentifier = null, string productName = null) {
			BuildPostProcessor.UsesNonExemptEncryption = usesNonExemptEncryption;

			this.buildNumber = Mathf.Max(1, buildNumber);
			this.bundleIdentifier = bundleIdentifier;
			this.productName = productName;
			buildPlayerOptions = new BuildPlayerOptions();
			buildPlayerOptions.options = developmentBuild ? BuildOptions.Development : BuildOptions.None;
			//buildPlayerOptions.options |= EditorUserBuildSettings.exportAsGoogleAndroidProject ? BuildOptions.AcceptExternalModificationsToPlayer : BuildOptions.None;
			//buildPlayerOptions.options = buildPlayerOptions.options | BuildOptions.AcceptExternalModificationsToPlayer;
			buildPlayerOptions.scenes = EditorBuildSettings.scenes.Select(s => s.path).ToArray();
			buildPlayerOptions.target = EditorUserBuildSettings.activeBuildTarget;
			buildPlayerOptions.locationPathName = ConfigureOutputLocation(outputPath);
		}

		string ConfigureOutputLocation(string outputPath) {
			if (IsAndroidBuild()) {
				if (!IsProjectExport(buildPlayerOptions.options)) {
					if (!outputPath.EndsWith(".apk")) {
						return Path.Combine(outputPath, (string.IsNullOrEmpty(productName) ? PlayerSettings.productName : productName) + ".apk");
					}
				}
#if UNITY_2019_1_OR_NEWER
				return Path.Combine(outputPath, "gradle-project");
#else
				return outputPath;
#endif
			}
			return Path.Combine(outputPath, "xcode-project");
		}

		static bool IsProjectExport(BuildOptions options) {
			return options.HasFlag(BuildOptions.AcceptExternalModificationsToPlayer);
		}

		bool IsAndroidBuild() {
			return buildPlayerOptions.target == BuildTarget.Android;
		}

		public void Execute() {
			PlayerSettings.iOS.buildNumber = buildNumber.ToString();
			PlayerSettings.Android.bundleVersionCode = buildNumber;
			if(!string.IsNullOrEmpty(bundleIdentifier)) {
				PlayerSettings.SetApplicationIdentifier(EditorUserBuildSettings.selectedBuildTargetGroup, bundleIdentifier);
			}
			if(!string.IsNullOrEmpty(productName)) {
				PlayerSettings.productName = productName;
			}
			BuildReport buildReport = BuildPipeline.BuildPlayer(buildPlayerOptions);
			RenameOutputGradleProject();
			if (!DidBuildSucceed()) {
				throw new System.Exception("Build Failed");
			}

			bool DidBuildSucceed() {
				return buildReport.summary.result == BuildResult.Succeeded;
			}

			void RenameOutputGradleProject() {
				if (IsAndroidBuild() && IsProjectExport(buildPlayerOptions.options)) {
					string pathToProject = Path.Combine(buildPlayerOptions.locationPathName, PlayerSettings.productName);
					if (Directory.Exists(pathToProject)) {
						Directory.Move(pathToProject, Path.Combine(buildPlayerOptions.locationPathName, "gradle-project"));
					}
				}
			}
		}

		public class BuildProcessor : IPreprocessBuildWithReport {
            public int callbackOrder => 0;

            public void OnPreprocessBuild(BuildReport report) {
				IPTechBuildInfo buildInfo = Resources.Load<IPTechBuildInfo>("IPTechBuildInfo");
				if(buildInfo == null) {
					buildInfo = ScriptableObject.CreateInstance<IPTechBuildInfo>();
					AssetDatabase.CreateAsset(buildInfo, Path.Combine("Assets", "Resources", "IPTechBuildInfo.asset"));
                }
				string buildNumber = PlayerSettings.iOS.buildNumber;
				if(EditorUserBuildSettings.activeBuildTarget == BuildTarget.Android) {
					buildNumber = PlayerSettings.Android.bundleVersionCode.ToString();
                }

				buildInfo.BuildNumber = buildNumber;
				AssetDatabase.SaveAssets();
            }
        }

		public class AndroidBuildProcessor : IPreprocessBuildWithReport, IPostGenerateGradleAndroidProject {
			const string MSG_CREATE_GRADLE_SETTINGS = "Adding a placeholder settings.gradle file so this project will build without detecting the parent gradle project during warmup";
			const string GRADLE_SETTINGS = "// placeholder settings.gradle file so that this gradle project does not detect the parent gradle project during warmup\n" +
				"include 'StagingArea'\n" +
				"include 'gradleOut'\n" +
				"include 'StagingArea:gradleWarmupArea'";

			BuildReport buildReport;
			public int callbackOrder { get { return int.MinValue; } }

			public void OnPostGenerateGradleAndroidProject(string path) {
				try {
					string outputPath = CalculateWrapperOutputPath();
					AddGradleWrapperToPath(outputPath);
				} catch (Exception e) {
					Debug.LogException(e);
					if (!UnityEditorInternal.InternalEditorUtility.inBatchMode) {
						EditorUtility.DisplayDialog("Error Post Processing Project", e.Message, "Ok");
					} else {
						EditorApplication.Exit(1);
					}
					throw new BuildFailedException(e.Message);
				}

				string CalculateWrapperOutputPath() {
#if UNITY_2019_1_OR_NEWER
					return Path.GetFullPath(Path.Combine(path, ".."));
#else
					return path;
#endif
				}
			}

			public void OnPreprocessBuild(BuildReport report) {
				try {
					buildReport = report;
					if (IsAndroidBuild()) {
						CreateTempGradleSettingsFile();
					}
				} catch (Exception e) {
					if (!UnityEditorInternal.InternalEditorUtility.inBatchMode) {
						//EditorUtility.DisplayDialog("Error Preprocessing Project", e.Message, "Ok");
					} else {
						//EditorApplication.Exit(1);
					}
					throw new BuildFailedException(e);
				}

				bool IsAndroidBuild() {
					return buildReport.summary.platform == BuildTarget.Android;
				}

			}

			void CreateTempGradleSettingsFile() {
				if (!HasGradleSettingsFile()) {
					Console.Out.WriteLine(MSG_CREATE_GRADLE_SETTINGS);
					Directory.CreateDirectory(GetGradleSettingsDir());
					File.WriteAllText(GetGradleSettingsPath(), GRADLE_SETTINGS);
				}

				bool HasGradleSettingsFile() {
					return File.Exists(GetGradleSettingsPath());
				}

				string GetGradleSettingsDir() {
					return Path.Combine(Application.dataPath, "..", "Temp");
				}

				string GetGradleSettingsPath() {
					return Path.Combine(GetGradleSettingsDir(), "settings.gradle");
				}
			}

			void AddGradleWrapperToPath(string outputPath) {
				Console.Out.WriteLine("Adding gradle wrapper to exported project at " + outputPath);
				string gradleLauncherPath;

				FindUnityGradleLauncher();
				AddGradleSettingsFile();
				ExecuteGradleWrapper();

				void FindUnityGradleLauncher() {
					string unityGradlePath = Path.Combine(EditorApplication.applicationPath, "..", "PlaybackEngines", "AndroidPlayer", "Tools", "gradle", "lib");
					if (Application.platform == RuntimePlatform.LinuxEditor) {
						unityGradlePath = Path.Combine(EditorApplication.applicationPath, "..", "Data", "PlaybackEngines", "AndroidPlayer", "Tools", "gradle", "lib");
					}

					string[] launcherSearch = Directory.GetFiles(unityGradlePath, "gradle-launcher-*.jar");
					if (launcherSearch != null && launcherSearch.Length > 0) {
						gradleLauncherPath = launcherSearch[0];
						return;
					}
					throw new FileNotFoundException("Could not find the unity gradle files");
				}

				void AddGradleSettingsFile() {
					string gradleSettingsFile = Path.Combine(outputPath, "settings.gradle");
					if (!File.Exists(gradleSettingsFile)) {
						File.WriteAllText(gradleSettingsFile,
							"rootProject.name='" + PlayerSettings.productName + "'"
#if UNITY_2019_1_OR_NEWER
							+ "\ninclude \"unityLibrary\""
							+ "\ninclude \"launcher\""
#endif
						);
					}
				}

				void ExecuteGradleWrapper() {
					int exitCode = new ShellCommand().ExecBash(
						string.Format("java -classpath \"{0}\" org.gradle.launcher.GradleMain wrapper", gradleLauncherPath),
						outputPath
					);

					if (exitCode != 0) throw new Exception("Failed to generate gradle wrapper for exported project.");
				}
			}

			class TmpFiles : IDisposable {
				string[] filePaths;
				public TmpFiles(params string[] filePaths) {
					this.filePaths = filePaths;
				}

				public void Dispose() {
					if (filePaths != null) {
						foreach (var filePath in filePaths) {
							try {
								if (File.Exists(filePath)) {
									File.Delete(filePath);
								}
							} catch { }
						}
					}
				}
			}
		}
	}
}
