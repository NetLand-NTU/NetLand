# NetLand
A comprehensive tool for quantitative modeling and visualization of <b>Waddington’s epigenetic landscape</b>

Waddington’s epigenetic landscape is a powerful metaphor for cellular dynamics driven by gene regulatory networks. Its quantitative modeling and visualization, however, remains a challenge, especially when there are more than two genes. Here, we present NetLand, an open-source software tool for modeling and simulating the kinetic dynamics of transcriptional regulatory networks with far more than two genes, and visualizing the corresponding Waddington’s epigenetic landscape. With an interactive and graphical user interface, NetLand can facilitate the knowledge discovery and experimental design in the study of cell fate regulation (e.g. stem cell differentiation and reprogramming). 

The software NetLand is written in Java, with a graphical user interface (GUI). For detailed instruction, please refer to the user manual.


### Release Information
Current release is 1.0.


### Installation
The Java Platform, Standard Edition Runtime Environment (JRE) is required to be installed (Java SE is available at http://java.com/en/download/inc/windows_upgrade_ie.jsp). We have tested NetLand under Window 7/10, Linux Fedora 18 and Mac OS 10.8.  

The current NetLand package includes the following files.
  
  1)	Two folders namely "toy models" and "saved results" which contain toy gene network files and pre-computed results respectively. 
  
  2)	A folder namely "lib". It contains the required libraries to launch NetLand. 
  
  3)	A folder namely "GPDM" under 'lib' which contain the executive files required for running "GPDM" program in NetLand. Make sure you have the executive permissions to the executive files. To check the permission, the command is `ls -l filename`. Use `chmod 705 filename` to gain the executive permission. 
  
  4)	Two scripts, "runNetLand.bat" and "runNetLand.sh", for launching NetLand in different operation systems. 
  
  Note that the scripts must be in the same directory as the "lib" folder. 
  
  To run the “GPDM” program, DLL (Dynamic Link Library) of BLAS, LAPACK and GFORTRAN are required. The DLL files for Windows OS are in the GPDM/win folder. For Mac OS, please check if libblas.dylib, liblapack.dylib and libgfortran.dylib are under /usr/lib/. Otherwise users should install the libraries themselves. 
  
  The BLAS, LAPACK and GFORTRAN can be downloaded from 
    
      •	LAPACK, see http://www.netlib.org/lapack/ 
      
      •	BLAS, see http://www.netlib.org/blas/ 
      
      •	GFORTRAN, see https://gcc.gnu.org/wiki/GFortranBinaries#MacOS 
  Then follow the install instructions in the package. For Linux, users can use the command, e.g. yum, to install these packages.   
  
### launch NetLand
  
      •	In Windows, double click the `runNetLand.bat`. 
      
      • 	In Linux, command `./runNetLand.sh`. 
      
      •	In OSX, command `./runNetLand.sh`.










