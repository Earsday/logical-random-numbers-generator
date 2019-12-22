# Logical random numbers generator
## Introduction
Sometimes, we need __a group of random numbers with certain logic among them__, and __the sum of these numbers should be equal to specified value__. So obviously, the generator for pure random numbers is not enough in this case. 

This tool is aimed to fulfill this kind of requirement. Currently, the latest version supports:
* random integers in a specified range, like '0~4' means a random integer between 0 and 4;
* math expressions supported by [exp4j](https://www.objecthunter.net/exp4j/), but so far, only one variable in the case of this project, which could be extended to multiple variables in future;

## Usage
### 1. Prepare number definitions
To simplify the inputs, this tool supports csv file as input file, which could be modified by any text editors or MS Excel. Its filename is allowed to be changed in the configuration file 'settings.ini'.
The input csv file needs at least 2 columns 'score' & 'sum' which determine the requirements of generated numbers. For example, you need 2 groups of numbers whose sum are 5 & 4, in each group, there are 2 random numbers whose ranges are '0~4' & '1~3'. Now you need to configure the following expressions into the input csv file:  
| sum | score |
|:---:|:-----:|
|  5  |  0~4  |
|  4  |  1~3  |
There is a sample input csv file 'input.scv' in this project.

### 2. Run the tool
#### 2.1 in Java IDE
Run the java class 'Version4', which is the latest version of the tool. You would see the logs and result in console. In the end, an output csv file would also be generated or overwritten. The name of the output file is also allowed to be configured & changed in the 'settings.ini'.
#### 2.2 in command line
You need to build an assembly jar in advance, then use the command 'java -jar xxx.jar' to run this tool directly, which would be faster then in IDE. To know how to assembly jar, please refer to the section 'Assembly'.

## Assembly 
With the support from maven-assembly-plugin, all dependencies would be packaged into the jar file '\target\logical-random-numbers-generator-jar-with-dependencies.jar', which can be launched directly through 'java -jar'. What you need to do is that run 'mvn clean package'.
