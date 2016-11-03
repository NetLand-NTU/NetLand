# NetLand
A software for quantitative modeling and visualization of Waddington’s epigenetic landscape

NetLand is intended for modeling, simulation and visualization of gene regulatory networks (GRNs) and their corresponding quasi-potential landscapes. Users can import models of GRNs from a file (e.g. TSV, SBML format etc.), and manually edit the network structure. Then, NetLand will automatically encode differential equations for the kinetics of transcriptional regulations. The computational model will be used to simulate the dynamics of the input networks. Model equations and parameters can be easily modified. 

To display the 3D landscape for a network of more than two genes, NetLand allows users to either choose two marker genes, or project to a latent space using a dimension reduction method called GPDM (Gaussian process dynamical model). Therefore, NetLand can provide a global picture of cellular dynamics for a user-specified GRN. Although we designed the NetLand software originally for modeling stem cell fate transitions, it can be also used to study other cellular phenotypes, such as cancer cell death, cellular ageing, etc..


# Documentary 
The software NetLand is written in Java, with a graphical user interface (GUI).
<b>The user manual contains detailed instruction about installation and basic usage. It also includes running time assessment and case studies (see https://github.com/NetLand-NTU/NetLand/blob/master/NetLand_manual.pdf). </b>

### Release Information
Current release is 1.0.


### Installation
The Java Platform, Standard Edition Runtime Environment (JRE) is required to be installed (Java SE is available at http://java.com/en/download/inc/windows_upgrade_ie.jsp). We have tested NetLand under Window 7/10, Linux Fedora 18 and Mac OSX 10.8.  

#### Dependencies for “GPDM” program
To run the “GPDM” program, libraries of BLAS, LAPACK and GFORTRAN are required. 

<b>Windows OS</b>: The DLL (Dynamic Link Library) files are provided in the “GPDM/win” folder. Mac OS X: Please check if libblas.dylib, liblapack.dylib and libgfortran.dylib are already installed in the system. The default installation path is /usr/lib/. Otherwise users should install the libraries themselves. 

<b>Unix/Linux</b>: Please check if libblas.so, liblapack.so and libgfortran.so are already installed in the system. The default installation path is /usr/lib/. Otherwise users should install the libraries themselves.

NOTE: the actual installation directory of the three libraries could be different. To check if they are installed, use a terminal, run: “locate <library>”.

To install the dependencies, the BLAS, LAPACK and GFORTRAN can be downloaded from
     •	LAPACK, see http://www.netlib.org/lapack/ 
      
      •	BLAS, see http://www.netlib.org/blas/ 
      
      •	GFORTRAN, see https://gcc.gnu.org/wiki/GFortranBinaries#MacOS 

Then follow the install instructions in the package. For Linux, users can use the command, e.g. yum, apt-get, to install these packages.   


<b>UNIX/Linux and Mac OS X</b>

1.	Download NetLand from GitHub: http://netland-ntu.github.io/NetLand/. 

2.	Unzip it to your desired location.

    The NetLand package should include the following files: 

        a)	One shell script, named "runNetLand.sh", for launching NetLand in Linux/Unix operating systems.

        b)	A jar file, named “main.jar”. It should be in the same directory as the shell script. 

        c)	A folder, named "lib". It contains the required libraries to launch NetLand.

        d)	A folder namely "GPDM" which contains the executable files required for running "GPDM" program in NetLand.
        
        e)	Two folders namely "toy models" and "saved results" (not necessary for launching the software) which contain toy gene network files and pre-computed results respectively.

3.	Make sure you have the executive permissions to the executable files in “GPDM” folder. To check the permission of a file, typing the following command at a terminal: `ls –l <filename>`. Use `chmod 705 <filename>` to gain the executive permission. 

4.	If everything installed correctly, you’re ready to run a simulation (see next section).

<br><br>
<b>Windows</b>

1.	Download NetLand from GitHub: http://netland-ntu.github.io/NetLand/. 

2.	Unzip it to your desired location.

    The NetLand package should include the following files: 

        a)	One shell script, named "runNetLand.bat", for launching NetLand in Windows operating systems.

        b)	A jar file, named “main.jar”. It should be in the same directory as the shell script. 

        c)	A folder, named "lib". It contains the required libraries to launch NetLand.

        d)	A folder namely "GPDM" which contains the executable files required for running "GPDM" program in NetLand.

        e)	Two folders namely "toy models" and "saved results" (not necessary for launching the software) which contain toy gene network files and pre-computed results respectively. 

3.	If everything installed correctly, you’re ready to run a simulation (see next section).

Note that the scripts must be in the same directory as the “GPDM” folder and "lib" folder.

    
    
### Launching NetLand

To launch NetLand:

     •	In Windows, double click the “runNetLand.bat”. 

     •	In Linux, run command “./runNetLand.sh” under a terminal.

     •	In Mac OS X, run command “./runNetLand.sh” under a konsole.

For Windows users, please check if "Java" is in your system path. If not, you can either add it to the system path or add the full path of "java.exe" to the script. 

Example: 

"C:\Program Files\Java\jre1.8.0_31\bin\java.exe" -classpath "lib\*;main.jar" WindowGUI.NetLand

For Mac OS X and Linux users, the script should be run under a terminal or konsole using the command "bash runNetLand.sh" or "./runNetLand.sh". Make sure you have the executive permissions to the scripts. To check the permission, the command is “ls –l <filename>”. Use “chmod 705 <filename>” to gain the executive permission. 

#### Memory requirement

NetLand uses the default settings of RAM in JVM. Minimal 20MB is required to launch NetLand. To manually change the RAM, users can add "-Xms<number>m –Xmx<number>m" to the shell scripts. This following command will allocate at least 2GB RAM and at most 4GB RAM to run NetLand.

Example: 

 java -Xms2048m -Xmx4096m -classpath "lib\*;main.jar " WindowGUI.NetLand 
 
The RAM requirement for this software can vary to different computational implementations, e.g. the iteration of simulations. The details of memory consumption can be found at Chapter 6 in the manual. 






