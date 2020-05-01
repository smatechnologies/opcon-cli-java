# OpCon CLI

OpCon CLI is a command line utility for Windows & Linux that uses the OpCon REST-API to interact with OpCon. Provides functions to manage machines, 
machine groups, jobs, properties, thresholds and schedules...

It consists of a single program **opconcli.exe** for Windows and **opconcli** for Linux.

## Installation

### Environement
- Uses the OpCon Rest-API to perform the functions, so an OpCon Rest-API license is required.

- The command lien utility needs **Java version 11** to function
  - An embedded JavaRuntimeEnvironment 11 is included along with the delivery zip / tar files. Once the archive extracted, "/java" directory contains the JRE binaries.

### Windows Instructions
Download OpConCLI_Windows.zip file from the desired [release available here](https://github.com/SMATechnologies/opcon-cli-java/releases).

After download, extract the zip file to the location you'd like to install the command line utility. Once unzipped, everything needed should be located under the root folder of that directory.

### Linux Instructions
Download OpConCLI_Linux.tar.gz file from the desired [release available here](https://github.com/SMATechnologies/opcon-cli-java/releases).

After download, extract the tar.gz file to the location you'd like to install the command line utility. Once extracted, everything needed should be located under the root folder of that directory.

## Configuration
The OpConCLI utility uses a configuration file **Connector.config** that contains the OpCon System connection information.

The header name of the OpCon System connection information is used by the **-o** command argument to retrieve the connection information for the required system. This allows the utility to submit requests to multiple OpCon systems from a single installation avoiding user to pass credentials every single time.

The **USER** and **PASSWORD** information should be encrypted using the **EncryptValue.exe** program for Windows or **EncryptValue** shell script for Linux (included in the zip files). The encryption tool provides basic encryption capabilities to prevent clear text.

**Connector.config** file example:
```
[GENERAL]
DEBUG=ON

[OPCON001]
SERVER=OPCON001
PORT=9010
USING_TLS=True
USER=62324e685a47303d
PASSWORD=6233426a6232353463484d3d

[OPCON002]
SERVER=OPCON002
PORT=9010
USING_TLS=True
USER=62324e685a47303d
PASSWORD=6233426a6232353463484d3d
```

Keyword | Type | Description
-------------- | ---- | -----------
SERVER | Text | The address or domain name of the OpCon Server
PORT | Number | The port being used by OpCon REST API server (usually 9010 or 9000)
USING_TLS | Boolean | Is OpCon REST API server is using TLS connections (either **True** / **False**)
USER | Text (encrypted) | Valid OpCon user encrypted using the EncryptValue program
PASSWORD | Text (encrypted) | Valid OpCon password associated with the OpCon user encrypted using the EncryptValue program

### EncryptValue Utility
The EncryptValue utility uses standard 64 bit encryption.

Supports a -v argument and displays the encrypted value

On Windows, example on how to encrypt the value "abcdefg":
```
EncryptValue -v abcdefg
```


## Command Line Arguments
The opconcli program requires arguments to be given to function. It uses the principle of Tasks, where each task perform an action or a combinaison of actions against OpCon.

### Global
Arguments | Description
--------- | -----------
**-o**  | (Mandatory) The name of the OpCon system to submit the request to. Matches a header value in the Connector.config file to identify the connection information for the task.

### AppToken
Can be used to create an application token.

Arguments | Description
--------- | -----------
**-t** | Value is **AppToken**
**-ap** | Required field for AppToken and represents the name of the application to create the token for. If a token for this application name already exists, it returns the existing value.

Usage
```
opconcli.exe -t AppToken -ap MY_CUSTOM_APP -o OPCONA
```

### Dependency
Can be used create a remote dependency on a job in another OpCon system. Tracks the job and returns the termination code of the job being tracked. Also returns the job log of the remote job.

Arguments | Description
--------- | -----------
**-t** | Value is **Dependency**
**-d** | Optional field that defines the Date of the request. If not present, the current date will be used. Date format is YYYY-MM-DD.
**-sn** | The name of the schedule in the Daily tables to add the job to.
**-jn** | The name of the job to add to the schedule in the Daily tables.

### GetJobLog
Retrieves the job log of an OpCon job.

Arguments | Description
--------- | -----------
**-t** | Value is **GetJobLog**
**-d** | Optional field that defines the Date of the request. If not present, the current date will be used. Date format is YYYY-MM-DD.
**-sn** | The name of the schedule in the Daily tables to add the job to.
**-jn** | The name of the job to add to the schedule in the Daily tables.
**-jld** | (**Optional**) field defining the full path name of the directory to write the job log into. The job log name created by the agent is used as the file name (i.e. JOB001_0000004245.log). If the field is not present the job log information will be added to the OpCon CLI execution output.

Example 1 : Retrieve job log for job TEST_ADD_JOB of schedule TEST_JOB_ADD for today’s date.
```
opconcli.exe -t GetJobLog -sn TEST_JOB_ADD -jn TEST_ADD_JOB -o OPCONA
```

Example 2 : Retrieve job log for job TEST_ADD_JOB tof schedule TEST_JOB_ADD for today’s date writing the log into file in directory c:\temp.
```
opconcli.exe -t GetJobLog -sn TEST_JOB_ADD -jn TEST_ADD_JOB -jld c:\temp -o OPCONA
```

### JobAction
Performs an action on a job on schedule in the daily tables.

Arguments | Description
--------- | -----------
**-t** | Value is **JobAction**
**-d** | Optional field that defines the Date of the request. If not present, the current date will be used. Date format is YYYY-MM-DD.
**-sn** | The name of the schedule in the Daily tables to add the job to.
**-jn** | The name of the job to add to the schedule in the Daily tables.
**-ja** | The action to be performed on the job. Possible values: **hold**, **cancel**, **skip**, **kill**, **start**, **restart**, **forceRestart**, **restartOnHold**, **release**, **markFinishedOk**, **markFailed**, **markUnderReview**, **markFixed**. Action is case-sensitive.

Example 1 : Cancel job TEST_ADD_JOB of schedule TEST_JOB_ADD for today’s date.
```
opconcli.exe -t JobAction -sn TEST_JOB_ADD -jn TEST_ADD_JOB -ja cancel -o OPCONA
```

### JobAdd
Adds a job to an existing schedule in the daily tables. Includes the capability to wait for the job that was added to complete.

Arguments | Description
--------- | -----------
**-t** | Value is **JobAdd**
**-d** | Optional field that defines the Date of the request. If not present, the current date will be used. Date format is YYYY-MM-DD.
**-sn** | The name of the schedule in the Daily tables to add the job to.
**-jn** | The name of the job to add to the schedule in the Daily tables.
**-jf** | The name of the frequency to be used for the job add request.
**-ip** | Optional field defining instance properties that should be passed to the job during the job add process. The format is **name=value,name=value**.
**-joh** | Flag to indicates to add the job in a hold status. If not present, the job will be added in a release status.
**-jw** | Flag to indicates to add the job and then track the execution of the job that was added.

Example 1 : Add job TEST_ADD_JOB to schedule TEST_JOB_ADD for today’s date using frequency AllDays and setting job instance properties TIME and ERROR.
```
opconcli.exe -t JobAdd -sn TEST_JOB_ADD -jn TEST_ADD_JOB -jf AllDays -ip TIME=15,ERROR=0 -o OPCONA
```

Example 2 : Add job TEST_ADD_JOB to schedule TEST_JOB_ADD for today’s date using frequency AllDays in a hold status and setting job instance properties TIME and ERROR.
```
opconcli.exe -t JobAdd -sn TEST_JOB_ADD -jn TEST_ADD_JOB -jf AllDays -ip TIME=15,ERROR=0 -o OPCONA -joh
```

Example 3 : Add job TEST_ADD_JOB to schedule TEST_JOB_ADD for today’s date using frequency AllDays, setting job instance properties TIME and ERROR and wait for job to complete before returning.
```
opconcli.exe -t JobAdd -sn TEST_JOB_ADD -jn TEST_ADD_JOB -jf AllDays -ip TIME=15,ERROR=0 -jw -o OPCONA
```

### MachAction
Performs an action on a machine. Includes a special value wlimited which sets a machine to limited mode and then waits for all current tasks on the machine to complete before returning.

Arguments | Description
--------- | -----------
**-t** | Value is **MachAction**
**-mn** | the machine(s) to perform the action on (value **mach1** for single machine or **mach1,mach2,mach3** for multiple machines).
**-ma** | the action to be performed on the machine(s). Value is **up**, **down**, **limited**, **wlimited**. (Note : wlimited is a special version of limited that sets machine to limited mode and returns when all the current running jobs have completed).

Example 1 : Set the machines MACH1,MACH2 to a down state.
```
opconcli.exe -t MachAction -mn BVHTEST02AMT_AGENT,AMT_AGENT -ma down -o OPCONA
```

### MachAdd
Create a new Machine(s) from a json template file. (Note : The json file structure is an array of machine definitions even if a single definition is submitted).  

Arguments | Description
--------- | -----------
**-t** | Value is **MachAdd**
**-md** | Fully Qualified DNS of the machine to be added. If present will override the value in the JSON file. (Note : only works if there is a single machine definition in the JSON file).
**-mf** | File name of JSON structure containing the definitions of the machine(s) to add (see below the json file structure).
**-mi** | IP Address of the machine to be added. If present will override the value in the JSON file. (Note : only works if there is a single machine definition in the JSON file).
**-mn** | Name of the machine to be added. If present will override the value in the JSON file. (Note : only works if there is a single machine definition in the JSON file).

**Machine json structure**:
```json
[
 {
  "requiresXMLEscape": false,
  "agentSMACommunicationsProtocol": "NEW2",
  "allowKillJob": false,
  "tcpIpAddress": "<Default>",
  "pollInterval": 1000,
  "checkMachineStatusInterval": 120,
  "connectionAttemptTimeout": 1000,
  "noBufferRetryCount": 40,
  "noBufferSleepTime": 250,
  "maxConsecutiveSendMessages": 100,
  "consecutiveSendSleepTime": 100,
  "sendBufferCount": 25,
  "receiveBufferCount": 25,
  "maxBytesSentPerMessage": 1024,
  "maxBytesReadPerMessage": 1024,
  "responseTimeout": 30,
  "agentCheckCRC": true,
  "closeSocketDuringSynchronization": false,
  "traceAllMessages": true,
  "smaNetComIdentifier": "<Default>",
  "gatewayName": "<None>",
  "jorsPortNumber": 3220,
  "fileTransferRole": "T",
  "fileTransferIPAddress": "<Default>",
  "availableProperties": [
    [
      {
        "name": "PowerShellPath",
        "value": "C:\\Windows\\system32\\WindowsPowerShell\\v1.0"
      },
      {
        "name": "MyMachineProp",
        "value": "MyMachineValue"
      }
    ]
  ],
  "fileTransferPortNumberForNonTLS": 3220,
  "fileTransferPortNumberForTLS": 0,
  "agentFileTransferPortNumberForTLS": "0",
  "lsamTime": 0,
  "lsamTimeDeltafromSAM": 0,
  "fullyQualifiedDomainName": "qa2012r2sam",
  "fullFileTransferSupport": false,
  "timeOffsetfromUTC": -5,
  "timeZoneName": "Central Standard Time",
  "timeOffsetfromSAMInHours": 0,
  "supportsHandshake": "False",
  "tlsCertificateDistinguishedName": "<Default>",
  "checkCertificateRevocationList": false,
  "supportTLSForSMAFTAgent": false,
  "agentSupportTLSForSMAFTAgent": "False",
  "supportTLSForSMAFTServer": false,
  "agentSupportTLSForSMAFTServer": "False",
  "supportNonTLSForSMAFTAgent": true,
  "agentSupportNonTLSForSMAFTAgent": "False",
  "supportNonTLSForSMAFTServer": true,
  "agentSupportNonTLSForSMAFTServer": "False",
  "supportedAgentCapability": [],
  "useTLSforSchedulingCommunications": false,
  "name": "MyWinAgent",
  "id": 88,
  "type": {
    "id": 3,
    "description": "Windows"
  },
  "socket": 3240,
  "gatewayId": 0,
  "groups": [],
  "lastUpdate": "2017-06-11T15:03:27.7480000-05:00",
  "maximumJobs": 0,
  "opconMaximumJobs": 0,
  "currentJobs": 0,
  "status": {
    "state": "D",
    "networkStatus": "D",
    "operationStatus": "D"
  }
 }
]

```

Example 1 : Create machine TEST001 in OpCon system OPCON from template win_machadd.json overriding name and address.
```
opconcli.exe -t MachAdd -mn TEST001 -mf c:\templates\win_machadd.json -mi 10.0.2.12 -o OPCON
```


### MachGrpAdd
Add a machine or machines to a machine group.

Arguments | Description
--------- | -----------
**-t** | Value is **MachGrpAdd**
**-mg** | The machine group name.
**-mn** | the machine(s) to perform the action on (value **mach1** for single machine or **mach1,mach2,mach3** for multiple machines).

Example 1 : Add machines TEST001 & TEST002 to machine group GRP001.
```
opconcli.exe -t MachGrpAdd -mg GRP001 -mn TEST001,TEST002 -o OPCONA
```

### MachGrpRemove
Remove a machine or machines from a machine group.

Arguments | Description
--------- | -----------
**-t** | Value is **MachGrpRemove**
**-mg** | The machine group name.
**-mn** | the machine(s) to perform the action on (value **mach1** for single machine or **mach1,mach2,mach3** for multiple machines).

### MachUpdate
Update the manchine name, ip address or fully qualified DNS name of an existing machine.

Arguments | Description
--------- | -----------
**-t** | Value is **MachUpdate**
**-mn** | the machine(s) to perform the action on (value **mach1** for single machine or **mach1,mach2,mach3** for multiple machines).
**-mnu** | The new name of the machine if the name is to be changed.
**-miu** | The new IP Address of the machine if the IP Address is to be updated (in this case the fully qualified DNS name will be set to '<Default>').
**-mdu** | The new DNS Address of the machine if the DNS Address is to be updated (in this case the IP Address will be set to <Default>).

### PropExp
Uses expression evaluation to create or update properties. Supports global and instance properties. When using instance properties use the fully qualified name.

Arguments | Description
--------- | -----------
**-t** | Value **PropExp** 
**-pn** | The name of the property to update.
**-pv** | The value of the property.

### PropUpdate
Updates the contents of a global property.

Arguments | Description
--------- | -----------
**-t** | Value is **PropUpdate**
**-pn** | The name of the property to update.
**-pv** | The value of the property.
**-pe** | If the property is encrypted (value **true** or **false**)

### SchedAction
Performs an action on the schedule in the daily.

Arguments | Description
--------- | -----------
**-t** | Value is **SchedAction**
**-d** | (**Optional**) field that defines the Date of the request. If not present, the current date will be used. Date format is YYYY-MM-DD.
**-sn** | The name of the schedule to update.
**-sa** | The action to be performed on the schedule. Value is **hold**, **release**, **start**, **close**. Action is case-sensitive.

### SchedBuild
Builds a schedule.

Arguments | Description
--------- | -----------
**-t** | Value is **SchedBuild**
**-d** | (**Optional**) field that defines the Date of the request. **If not present, the current date will be used. Date format is YYYY-MM-DD**.
**-sn** | The name of the schedule to build.
**-soh** | Flag that indicates to build schedule in a hold status. If not present, the schedule will be built in a release status.
**-ip** | (**Optional**) field defining instance properties that should be passed to the schedule during the build. The format of the instance properties is **name1=value1,name2=value2**.

Example 1 : Build the schedule TEST_JOB_ADD for today’s date on hold.
```
opconcli.exe -t SchedBuild -sn TEST_JOB_ADD -soh -o OPCONA
```

Example 2 : Build the schedule TEST_JOB_ADD for 12th May 2019 on hold setting schedule instance properties PROP1 & PROP2.
```
opconcli.exe -t SchedBuild -sn TEST_JOB_ADD -soh -d 2019-05-12 -ip PROP1=TEST,PROP2=ONE -o OPCONA
```

### SchedRebuild
Can be used to rebuild schedules in the daily from a given date for a number of days.

Arguments | Description
--------- | -----------
**-t** | Value is **SchedRebuild**
**-d** | (**Optional**) field that defines the Date of the request. **If not present, the current date will be used. Date format is YYYY-MM-DD**.
**-sd** | The number of days to rebuild the schedules for.
**-sri** | Is used to indicate which schedule should be rebuilt. Should be the first x number of characters of the schedule name or the entire name. **If the schedule names contains spaces remember to enclose the argument in double quotes**.

Example 1 : Rebuild schedules in OpCon system OPCON from 1st March for 5 days.
```
opconcli.exe -t SchedRebuild -d 2020-03-01 -o OPCON -sd 5
```

Example 2 : Rebuild schedules starting with TEST in OpCon system OPCON from 1st March for 5 days.
```
opconcli.exe -t SCHEDREBUILD -d 2020-03-01 -o OPCON -sd 5 -sri TEST
```

### ThreshUpdate
Updates the value of a threshold.

Arguments | Description
--------- | -----------
**-t** | Value is **ThreshUpdate**
**-tn** | The name of the threshold to update.
**-tv** | The value of the threshold.

Example 1 : Update threshold THRESH001 value to 55.
```
opconcli.exe -t ThreshUpdate -tn THRESH001 -tv 55 -o OPCONA
```

## Disclaimer
No Support and No Warranty are provided by SMA Technologies for this project and related material. The use of this project's files is on your own risk.

SMA Technologies assumes no liability for damage caused by the usage of any of the files offered here via this Github repository.
