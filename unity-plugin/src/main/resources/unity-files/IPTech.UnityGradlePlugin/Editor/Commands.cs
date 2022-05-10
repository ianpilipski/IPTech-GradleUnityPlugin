using System;
using UnityEditor;
using UnityEditor.Build.Reporting;
using System.Linq;
using System.Collections.Generic;

namespace IPTech.UnityGradlePlugin {
	public static class Commands
	{
		public static void Build() {
			try {
				var args = new CommandLineParser(Environment.GetCommandLineArgs());
				new Build(args.outputPath, args.developmentBuild, args.buildNumber).Execute();
			} catch(Exception e) {
				Console.Error.WriteLine(e.ToString());
				EditorApplication.Exit(1);
			}
		}
	}
}
