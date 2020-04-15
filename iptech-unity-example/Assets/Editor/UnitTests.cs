using System;
using System.Collections;
using NUnit.Framework;
using UnityEngine.TestTools;

public class UnitTests
{
    [Test]
    public void TestExample([Range(0,10)] int value)
	{
        Console.Out.WriteLine("value = " + value);
        Assert.IsTrue(value > -1);
	}

    [UnityTest]
    public IEnumerator UnityTestExample()
    {
        for(int i=0;i<10;i++)
        {
            Console.Out.WriteLine("value = " + i);
            yield return null;
        }
        Assert.IsTrue(true);
    }
}
