# OpCon CLI
A command line utility for Windows & Linux that uses the OpCon Rest-API to interact with OpCon. Provides functions to manage machines, 
machine groups, jobs, properties, thresholds and schedules.

[ SOURCE CODE COMMING SOON ]

# Prerequisites

- Uses the OpCon Rest-API to perform the functions, so an OpCon Rest-API license is required
- Requires **Java version 8**.
  - embedded OpenJDK included in installation zip / tar files.

# Instructions
Is a command utility that submits requests to OpCon using the SMA OpCon Rest-API. 
The utility is available for both Windows and Linux.

It consists of a single program **OpConCLI.exe** for Windows and **OpConCLI.sh** for Linux

## Supported Functions
 
Provides the following functions:

- **-AppToken**      : Used to create an application token
- **-Dependency**    : Used to check a job dependency on a remote OpCon System. It tracks the execution 
                       of the remote job waiting for job completion. Retrieves the job log of the remote 
                       job and terminates with the same termination code as the remote job (requires 
                       OpCon 17.1 or greater).
- **-GetJobLog**     : Retrieve the job log of a job in a schedule in the Daily tables (requires OpCon 17.1 or
                       greater).
- **-JobAction**     : JobAction	Performs an action on a job in the Daily tables (available actions are hold, 
                       cancel, skip, kill, start, restart, forceRestart, restartOnHold, release, markFinishedOk, 
                       markFailed, markUnderReview, markFixed).
- **-JobAdd**        : Add a job to schedule in the Daily tables. Includes an option to wait for the 
                       completion of the job that has been added.
- **-MachAction**    : Performs an action on a machine or list of machines (available actions are up, 
                       down, limited, wlimited). The option wlimited option, sets the machine into limited state
                       and waits until all current executing jobs on the machine to complete before returning.
- **-MachAdd**       : Add a machine or list of machines to the OpCon System.
- **-MachGrpAdd**    : Add the machines to the machine group.
- **-MachGrpRemove** : Removes the machines from the machine group.
- **-MachUpdate**    : Update machine name, IP address or DNS address.
- **-PropExp**       : Uses the expression evaluator to update properties. If the property does not 
                       exist it is created. Supports global and instance properties.
- **-PropUpdate**    : Updates the value of a global property for OpCon 18.1 and beyond (supports 
                       encryption).
- **-SchedAction**   : Performs an action on a schedule (available actions are hold, release, start,
                       close).
- **-SchedBuild**    : Build a schedule (requires OpCon 18.1 or greater).
- **-SchedRebuild**  : Rebuilds schedules in the daily (requires OpCon 18.1 or greater).
- **-ThreshUpdate**  : Updates the value of a threshold.
- **-Version**       : Retrieves the version of a the SMA OpCon-RestAPI

See documentation for arguments required for the individual functions.

## Installation

### Windows
 
Download OpConCLI_win.zip file from the desired release by selecting and saving the file.
After download create a installation root folder and then extract the information from the downloaded
file into the created installation directory. 

### Linux
 
Download OpConCLI_Linux.tar file from the desired release by selecting and saving the file.
After download create a installation root folder and then extract the information from the downloaded
file into the created installation directory.

See documentation for configuration requirements after installation.

# Disclaimer
No Support and No Warranty are provided by SMA Technologies for this project and related material. The use of this project's files is on your own risk.

SMA Technologies assumes no liability for damage caused by the usage of any of the files offered here via this Github repository.

# License
Copyright 2019 SMA Technologies

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at [apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

# Contributing
We love contributions, please read our [Contribution Guide](CONTRIBUTING.md) to get started!

# Code of Conduct
[![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg)](code-of-conduct.md)
SMA Technologies has adopted the [Contributor Covenant](CODE_OF_CONDUCT.md) as its Code of Conduct, and we expect project participants to adhere to it. Please read the [full text](CODE_OF_CONDUCT.md) so that you can understand what actions will and will not be tolerated.
