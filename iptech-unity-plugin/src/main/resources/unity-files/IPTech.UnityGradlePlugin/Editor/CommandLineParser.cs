using System.Collections.Generic;

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
    }
}
