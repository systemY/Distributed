package splitAndMerge;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

public class Merge 
{
	public static ArrayList<File> listOfFilesToMerge(String fileName) 
	{
		File oneOfFiles=new File(fileName);
	    String tmpName = oneOfFiles.getName();//{name}.{number}
	    String destFileName = tmpName.substring(0, tmpName.lastIndexOf('.'));//remove .{number}
	    File[] files = oneOfFiles.getParentFile().listFiles((File dir, String name) -> name.matches(destFileName + "[.]\\d+"));
	    Arrays.sort(files);//ensuring order 001, 002, ..., 010, ...
	    return (new ArrayList<File>(Arrays.asList(files)));
	}
	
	public static void mergeFiles(ArrayList<File> files, File into) throws IOException 
	{
	    try (BufferedOutputStream mergingStream = new BufferedOutputStream(
	            new FileOutputStream(into))) 
	    {
	        for (File f : files) 
	        {
	            Files.copy(f.toPath(), mergingStream);
	        }
	    }
	}
}