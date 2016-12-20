// Tiffany Chien
// 9/3/13
// OpenFile.java
// This program contains some methods for opening a text file for reading, and opening
// a text file for writing.

import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class OpenFile
{
	public static Scanner openToRead (String fileName)
	{
		Scanner fromFile = null;
		try
		{
			File myFile = new File (fileName);
			fromFile = new Scanner(myFile);
		}
		catch (FileNotFoundException e)
		{
			System.out.println("\n\nSorry, but the file could not be found\n\n");
			System.exit(1);
		}
		return fromFile;
	}
	
	public static PrintWriter openToWrite (String fileName)
	{
		PrintWriter toFile = null;
		try
		{
			toFile = new PrintWriter(fileName);
		}
		catch (Exception e)
		{
			System.out.println("\n\nSorry, but the file could not be opened for writing.\n\n");
			System.exit(1);
		}
		return toFile;
	}
}