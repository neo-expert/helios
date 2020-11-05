import js.*;
import js.event.*;
import static js.dom.DOM.*;
import js.dom.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import com.cupvm.jsjvm.*;
import omegadrive.SystemLoader;

public class Main{
	public Main(){
	
	}
	public static void main(String[] args)throws Throwable{
		//returns true in a Browser
		if(!DOM.isInitialized()){
			//DOM is not initialized,
			//starts the webserver
			JVMServer.startThisJar(args);
			return;
		}
		//this code will be executed in browser:
		System.out.println("hello world");
		SystemLoader.main(args);
	}
	@Override
	public String toString(){
		return "hello";
	}
        
}
