# InPaFer

* I. Introduction of quantitative study
* II. Environment setup
* III. How to run
* IV. Structure of the project

## I. Introduction
To evaluate the effectiveness of InPaFer, we conducted a quantitative study. Specially, we measured  ratio of incorrect patches that are successfully filtered out by InPaFer and the number of filtering steps under different filtering orders. We also conducted two sensitivity analyses for InPaFer to attributes and developers' errors. 


## II. Environment

* OS: macOS Catalina (version 10.15.3)
* JDK: Oracle jdk1.8 (**important!**)
* IDE: Eclipse (version 4.11.0)
* Download and configure Defects4J (**branch  [fee5ddf020](https://github.com/rjust/defects4j/tree/fee5ddf020d0ce9c793655b74f0ab068153c03ef)**) running environment.

## III. How to run

#### Before running

* Configure the following path in File [Constant.java](./src/main/java/config/Constant.java).

  * HOME="homoe_of_this_project"
  * PROJECT_HOME="home_of_data"
  * COMMAND_JAVA_HOME="home_of_java7"
  * COMMAND_D4J="home_of_defects4j_command"
  * D4J_HOME="home_of_defects4j"

* Download the project which you want to run to path `PROJECT_HOME/ProjectName/ProjectName_Bugid_buggy`.
  
  * Notice that the path of project need follow above format. For example, if you want to run math41, you may download the source code of math41 to `PROJECT_HOME/Math/Math_41_buggy`.


#### Step 1, Obtain the extra information

* Run the Main class `Main.PatchVariationMain`:

  `Run As`→`Run Configurations…` →`Arguments` : set the following arguments as Program Arguments.

    * `-project` : the project name of buggy program of benchmark. (`-project=Math` for the example)
   	* `-start` : the bug id of start running project. (`-start=41` for the example)
   	* `-end` : the bug id of end running project. (`-end=41` for the example)

   	After running the class `Main.PatchVariationMain`, you will obtain two directories: cache and record, for the next step.


#### Step 2, Obtain results of quantitative study

* RQ1: Effectiveness && Efforts
  * Run the Main class `experiment.RQ1`

    `Run As`→`Run Configurations` →`Arguments` : set the following arguments as Program Arguments.

    * `-project` : the project name of buggy program of benchmark. (`-project=Math` for the example)
    * `-start` : the bug id of start running project. (`-start=41` for the example)
    * `-end` : the bug id of end running project. (`-end=41` for the example)

    After running, you will obain eight files, recording the number of remaining patches after each filtering step under one filtering orders, respectively. For example, queryNumber_stra0.csv represents the results under random filtering order.

* RQ5: Sensitivity analysis to attributes
  * Run the Main class `experiment.RQ5`

    `Run As`→`Run Configurations…` →`Arguments` : set the following arguments as Program Arguments.

    * `-project` : the project name of buggy program of benchmark. (`-project=Math` for  example)
    * `-start` : the bug id of start running project. (`-start=41` for  example)
    * `-end` : the bug id of end running project. (`-end=41` for example)
    * `-t` :  the un-configurable attribute. (three options: `-t=method`, `-t=variable` and `-t=trace`)

    After running, you will obain one file, recording the number of remaining patches after each filtering step when configuring two attributes. For example, when setting `-t=method`, you will get queryNumber.csv which records the results when configuring variable and trace. 

* RQ6: Sensitivity analysis to error rates
	* Run the Main class `experiment.RQ6`

    `Run As`→`Run Configurations…` →`Arguments` : set the following arguments as Program Arguments.

    * `-project` : the project name of buggy program of benchmark. (`-project=Math` for  example)
    * `-start` : the bug id of start running project. (`-start=41` for  example)
    * `-end` : the bug id of end running project. (`-end=41` for example)

    After running, you will obain five files, recording the results whether InPaFer can filter all incorrect patches and corresponding filtering steps under one error rate, respectively. The error rates range from 0.02 to 0.1. For example, bugNumber_0.02.csv" represents the results under 0.02 of error rate.

#### Another running way
  
  This is a mvn project, so you can run using command line. 

  * Build project: `mvn clean -U package`
  * Run: cd target and run `java -jar interactive-patch-filter-1.1-SNAPSHOT-jar-with-dependencies.jar ` &&  the corresponding arguments.


Notice that all results have been run tem times.


## V. Structure

```powershell
  |--- README.md   :  user guidance
  |--- d4j-info    :  defects4j information
  |--- lib         :  dependent libraries
  |--- FinalPatch  :  the patches used in experiment
  |--- src         :  source code
```
----

