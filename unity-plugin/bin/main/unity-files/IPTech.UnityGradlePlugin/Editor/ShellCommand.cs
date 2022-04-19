using System;
using System.Diagnostics;
using UnityEngine;

namespace IPTech.UnityGradlePlugin {
	public class ShellCommand {

		public int ExecBash(String commands, String workingDirectory) {
			string shellcmd = "/bin/bash";
			string args = PrepareArguments();
			Process p = new Process();
			p.StartInfo.WindowStyle = ProcessWindowStyle.Normal;
			//p.StartInfo.CreateNoWindow = true;
			p.StartInfo.UseShellExecute = false;
			p.StartInfo.FileName = shellcmd;
			p.StartInfo.Arguments = args;
			p.StartInfo.WorkingDirectory = workingDirectory;
			p.EnableRaisingEvents = true;
			p.StartInfo.RedirectStandardError = true;
			p.StartInfo.RedirectStandardOutput = true;
			p.StartInfo.RedirectStandardInput = true;
			p.Start();

			p.StandardInput.WriteLine(commands);
			p.StandardInput.WriteLine("exit");
			p.StandardInput.Flush();
			p.StandardInput.Close();

			string stdOut = p.StandardOutput.ReadToEnd();
			string stdErr = p.StandardError.ReadToEnd();
			p.WaitForExit();

			if (!string.IsNullOrEmpty(stdOut)) {
				UnityEngine.Debug.Log(stdOut);
			}
			if (!string.IsNullOrEmpty(stdErr)) {
				UnityEngine.Debug.LogError(stdErr);
			}

			return p.ExitCode;
		}

		string PrepareArguments() {
			if (Application.platform == RuntimePlatform.WindowsEditor) {
				throw new NotImplementedException();
			} else {
				if (UnityEditorInternal.InternalEditorUtility.inBatchMode) {
					return "-l";
				}
				return string.Empty;
			}
		}
	}
}
