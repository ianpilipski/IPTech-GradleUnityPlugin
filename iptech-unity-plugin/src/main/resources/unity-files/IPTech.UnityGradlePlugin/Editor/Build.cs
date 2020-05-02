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
    public class Build  {
		BuildPlayerOptions buildPlayerOptions;

		public Build(string outputPath, bool developmentBuild) {
			buildPlayerOptions = new BuildPlayerOptions();
			buildPlayerOptions.options = developmentBuild ? BuildOptions.Development : BuildOptions.None;
			buildPlayerOptions.scenes = EditorBuildSettings.scenes.Select(s => s.path).ToArray();
			buildPlayerOptions.target = EditorUserBuildSettings.activeBuildTarget;
			buildPlayerOptions.locationPathName = ConfigureOutputLocation(outputPath);
		}

		string ConfigureOutputLocation(string outputPath) {
			if (IsAndroidBuild()) {
				if (!IsProjectExport()) {
					if (!outputPath.EndsWith(".apk")) {
						return Path.Combine(outputPath, PlayerSettings.productName + ".apk");
					}
				}
				
				return Path.Combine(outputPath, "gradle-project");
			}
			return Path.Combine(outputPath, "xcode-project");

			bool IsProjectExport() {
				return buildPlayerOptions.options.HasFlag(BuildOptions.AcceptExternalModificationsToPlayer);
			}

			bool IsAndroidBuild() {
				return buildPlayerOptions.target == BuildTarget.Android;
			}
		}

		public void Execute() {
			BuildReport buildReport = BuildPipeline.BuildPlayer(buildPlayerOptions);
			if(!DidBuildSucceed()) {
				throw new System.Exception("Build Failed");
			}

			bool DidBuildSucceed() {
				return buildReport.summary.result == BuildResult.Succeeded;
			}
		}

		
		public class AndroidBuildProcessor : IPreprocessBuildWithReport {
			const string MSG_CREATE_GRADLE_SETTINGS = "Adding a placeholder settings.gradle file so this project will build without detecting the parent gradle project during warmup";
			const string GRADLE_SETTINGS = "// placeholder settings.gradle file so that this gradle project does not detect the parent gradle project during warmup\n" +
				"include 'StagingArea'\n" +
				"include 'gradleOut'\n" +
				"include 'StagingArea:gradleWarmupArea'";

			BuildReport buildReport;
			public int callbackOrder { get { return int.MinValue; } }
			
			public void OnPreprocessBuild(BuildReport report) {
				try {
					buildReport = report;
					if (IsAndroidBuild()) {
						if (!IsProjectExport()) {
							CreateTempGradleSettingsFile();
						}
					}
				} catch(Exception) {
					EditorApplication.Exit(1);
					throw;
				}

				bool IsAndroidBuild() {
					return buildReport.summary.platform == BuildTarget.Android;
				}

				bool IsProjectExport() {
					return buildReport.summary.options.HasFlag(BuildOptions.AcceptExternalModificationsToPlayer);
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
		}
	}
}
