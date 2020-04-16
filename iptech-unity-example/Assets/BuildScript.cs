using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;

public static class BuildScript
{
    static void HelloWorld()
    {
        Console.Out.WriteLine("You just called a unity method named HelloWorld!");
    }

    static void WithArguments()
    {
        Console.Out.WriteLine("You just called a unity method named WithArguments!");
        List<String> args = Environment.GetCommandLineArgs().ToList();

        String value = args[args.IndexOf("-Hello") + 1];
        Console.WriteLine("Found argument -Hello with value of " + value);
    }
}
