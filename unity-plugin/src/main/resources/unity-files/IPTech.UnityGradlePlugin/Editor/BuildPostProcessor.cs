using UnityEditor;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;
#if UNITY_IOS
using System.IO;
using UnityEditor.iOS.Xcode;
#endif

namespace IPTech.UnityGradlePlugin {
    public class BuildPostProcessor : IPostprocessBuildWithReport {
        static public bool UsesNonExemptEncryption = false;

        public int callbackOrder { get { return 0; } }

        public void OnPostprocessBuild(BuildReport report) {
            OnPostProcessBuildIOS(report);
        }


        void OnPostProcessBuildIOS(BuildReport report) {
#if UNITY_IOS
            if(UsesNonExemptEncryption) {
                if(report.summary.platform == BuildTarget.iOS) {
                    string plistPath = report.summary.outputPath + "/Info.plist";

                    PlistDocument plist = new PlistDocument();
                    plist.ReadFromString(File.ReadAllText(plistPath));

                    PlistElementDict rootDict = plist.root;
                    rootDict.SetBoolean("ITSAppUsesNonExemptEncryption", false);

                    File.WriteAllText(plistPath, plist.WriteToString()); // Override Info.plist
                }
            }
#endif
        }
    }
}

