# NetLand
A software for quantitative modeling and visualization of Waddington’s epigenetic landscape

NetLand is intended for modeling, simulation and visualization of gene regulatory networks (GRNs) and their corresponding quasi-potential landscapes. Users can import models of GRNs from a file (e.g. TSV, SBML format etc.), and manually edit the network structure. Then, NetLand will automatically encode differential equations for the kinetics of transcriptional regulations. The computational model will be used to simulate the dynamics of the input networks. Model equations and parameters can be easily modified. 

To display the 3D landscape for a network of more than two genes, NetLand allows users to either choose two marker genes, or project to a latent space using a dimensionality reduction method called GPDM (Gaussian process dynamical model). Therefore, NetLand can provide a global picture of cellular dynamics for a user-specified GRN. Although we designed the NetLand software originally for modeling stem cell fate transitions, it can be also used to study other cellular phenotypes, such as cancer cell death, cellular ageing, etc..


# Documentary 
The software NetLand is written in Java, with a graphical user interface (GUI). The user manual contains detailed instruction about installation and basic usage. It also includes running time and memory usage assessment and case studies (https://github.com/NetLand-NTU/NetLand/blob/master/NetLand_manual.pdf). 

### Release Information
Current release is 1.0.


### Installation
The Java Platform, Standard Edition Runtime Environment (JRE) 1.7 or higher version is required to be installed (Java SE is available at http://java.com/en/download/inc/windows_upgrade_ie.jsp). We have tested NetLand under 64-bit version of Windows 7 and Windows 10, Linux Fedora 18 and Mac OS X 10.8 Mountain Lion.  

#### Dependencies for “GPDM” program
To run the “GPDM” program, libraries of BLAS, LAPACK and GFORTRAN are required. 

<b>Windows OS</b>: The DLL (Dynamic Link Library) files are provided in the “GPDM/win” folder. 

<b>Mac OS X</b>: Please check if libblas.dylib, liblapack.dylib and libgfortran.dylib are already installed in the system. The default installation path is /usr/lib/. Otherwise users should install the libraries themselves. 

<b>Unix/Linux</b>: Please check if libblas.so, liblapack.so and libgfortran.so are already installed in the system. The default installation path is /usr/lib/. Otherwise users should install the libraries themselves.

NOTE: the actual installation directory of the three libraries might not be the default. To check if they are installed, use a terminal and run: “locate <library>”.

To install the dependencies, the BLAS, LAPACK and GFORTRAN can be downloaded from

     •	LAPACK: http://www.netlib.org/lapack/ 
      
      •	BLAS: http://www.netlib.org/blas/ 
      
      •	GFORTRAN: https://gcc.gnu.org/wiki/GFortranBinaries#MacOS 

Then follow the install instructions in the package. For Linux, users can use command line tools for package management, e.g. yum, apt-get, to install these packages.      


### Steps of installation 

1.	Download the compressed folder, named NetLand.zip, from GitHub: http://netland-ntu.github.io/NetLand/. 

2.	Unzip it to your desired location.

     The NetLand package should include the following files and folders: 

     a)	“runNetLand.sh” (Linux/Unix or Mac OS X) and “runNetLand.bat” (Windows): Shell scripts for launching NetLand on the different operating systems.
     
     b)	“main.jar”: A jar file that contains aggregate Java class files of NetLand which should be in the same directory with the shell script. 
     
     c)	“lib”: A folder that contains the required libraries to launch NetLand.
     
     d)	“GPDM”: A folder that contains the executable files required for running the "GPDM" program to do dimensionality reduction in NetLand.
     
     e)	Two folders, namely “toy models” and “saved results” (not necessary for launching the software), which contain files of toy network models and pre-computed results respectively.
     
3.   Make sure you have the execute permissions to the executable files in “GPDM” folder under Linux/Unix or Mac OS X. To check the permission of a file, type the following command at a terminal: “ls –l <filename>”. Use “chmod 705 <filename>” to obtain the execute permission.


If everything is installed correctly, you are ready to run a simulation (see next section). <b>Note that the scripts (“runNetLand.sh” and “runNetLand.bat”) must be in the same directory with the “GPDM” folder and the "lib" folder.</b>
    
    
### Launching NetLand

To launch NetLand:

     •	Under Windows, double click the “runNetLand.bat”.  

     •	Under Linux, run the command “./runNetLand.sh” in a terminal.

     •	Under Mac OS X, run the command “./runNetLand.sh” in a konsole.

For Windows users, please check if "Java" has been set in your system path. If not, you can either add it to the system path or add the full path of "java.exe" to the script (“runNetLand.bat”). To check and update the system path under Windows, you may follow the steps below: 

1.	From the desktop, right click the Computer icon.
2.	Choose Properties from the context menu.
3.	Click the Advanced system settings link.
4.	Click Environment Variables. In the section System Variables, find the PATH environment variable and select it. Click Edit and check if the path of Java is in the “Variable value”. Generally, the full path of Java program looks like “C:\Program Files\Java\jre<version>\bin”.  
5.	If Java is not in the system path, then please add the path of Java to the PATH environment variable in the Edit System Variable (or New System Variable) window. Click OK. Close all remaining windows by clicking OK.

Another way to run the software without changing of the system path is to update the shell script (“runNetLand.bat”) by specifying the full path of Java. This script can be edited using text editors, e.g. WordPad, Notepad. For example: 

Example: 

"C:\Program Files\Java\jre1.8.0_31\bin\java.exe" -classpath "lib\*;main.jar" WindowGUI.NetLand

For users of Mac OS X and Linux/UNIX, the script should be run in a terminal or konsole using the command "bash runNetLand.sh" or "./runNetLand.sh". Make sure you have the execute permissions on the script. To check the permission, the command is “ls –l <filename>”. Use “chmod 705 <filename>” to obtain the execute permission. 


#### Memory requirement

NetLand runs on JVM (Java virtual machine) using the default settings of memories. To manually change the RAM, users can add the command line arguments "-Xms<number>m –Xmx<number>m" to the shell scripts (“runNetLand.sh” and “runNetLand.bat”) using text editors. “-Xms” stands for the minimum heap size, and “-Xmx” denotes the maximum heap size. For example, the following command allows a minimum of 2GB and a maximum of 4GB of memories used by the JVM: 

 java -Xms2048m -Xmx4096m -classpath "lib\*;main.jar " WindowGUI.NetLand 
 
The memory requirement depends on the size of network model and values of parameters, e.g. the number of iterations in a simulation. A minimum memory of 20MB is required to launch NetLand. The details of memory consumption can be found in Chapter 7 of the user manual.  






