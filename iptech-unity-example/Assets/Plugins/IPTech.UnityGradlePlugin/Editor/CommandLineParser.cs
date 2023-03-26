using System.Collections.Generic;
using UnityEditor;

namespace IPTech.UnityGradlePlugin {
	public class CommandLineParser {
        public readonly IDictionary<string, string> Arguments;

        public CommandLineParser(string [] args) {
            Arguments = new Dictionary<string, string>();

            string token = null;
			foreach(var arg in args) {
				if(arg.StartsWith("-")) {
					if(token!=null) {
						Arguments.Add(token, null);
					}
					token = arg;
				} else {
					if(token!=null) {
						Arguments.Add(token, arg);
						token = null;
					}
				}
			}
			//if the last arg was a switch it will be here
            if(token!=null) {
                Arguments.Add(token, null);
            }
		}

		public bool developmentBuild {
			get {
				return Arguments.ContainsKey("-developmentBuild");
			}
		}

		public string outputPath {
			get {
				string[] checkSwitches = { "-outputFile", "-outputDir", "-outputPath" };
				foreach (var s in checkSwitches) {
					if (Arguments.TryGetValue(s, out string value)) {
						if (!string.IsNullOrEmpty(value)) {
							return value;
						}
					}
				}
				throw new System.ArgumentNullException("-outputPath must be specified with a valid path");
			}
		}

		public int buildNumber {
			get {
				if(Arguments.TryGetValue("-buildNumber", out string val)) {
					return int.Parse(val);
				}
				if(UnityEngine.Application.platform == UnityEngine.RuntimePlatform.Android) {
					return PlayerSettings.Android.bundleVersionCode;
				} else {
					return int.Parse(PlayerSettings.iOS.buildNumber);
				}
			}
        }

		public bool usesNonExemptEncryption {
			get {
				return Arguments.ContainsKey("-exemptEncryption");
            }
        }

		public string bundleIdentifier {
			get {
				if(Arguments.TryGetValue("-bundleIdentifier", out string val)) {
					return val;
				}
				return null;
			}
		}

		public string productName {
			get {
				if(Arguments.TryGetValue("-productName", out string val)) {
					return val;
				}
				return null;
			}
		}

		public bool buildAsAppBundle {
			get {
				return Arguments.TryGetValue("-buildAsAppBundle", out string _);
            }
        }

		public string keyStoreSettingsFile {
			get {
				if(Arguments.TryGetValue("-keyStoreSettings", out string val)) {
					return val;
                }
				return null;
            }
        }
    }
}
