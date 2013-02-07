# Solenopsis #

A commandline interface for deploying to Salesforce

Currently you will need to download the ant-salesforce.jar to the ant/lib/ directory.  We will be working on making this an automated process as well as phasing out the ant library for the MetadataAPI.

## Why Solenopsis? ##

The Salesforce Ant Deployment Tool will get metadata into and out of your org. This is useful, but it is far from being useful when managing code from several orgs.

Solenopsis is built on the Ant Deployment Tool to extend its functionality. Solenopsis adds metadata diffing and pushing only metadata files chanced since last Git commit.

## Solenopsis Terms ##

Master = Your copy of org's metadata. A local directory.

Dependent = Someone else's org metadata. A local directory.

Folder-based = Individual folders in an unzipped metadata directory. Example: 'applications', 'classes', or 'components'


## What can Solenopsis do? ##

### Push to Salesforce org ###

Destructive push to dependent org from master org. (from git)
- `ant -f ${ant.file} git-destructive-push`

Push to dependent org from master org. (from git)
- `ant -f ${ant.file} git-push`

Push local files specified by the 'sf.files2push' property to dependent org.
Can also set the files on the command-line.
- `ant -f ${ant.file} file-push`
- `ant -Dsf.files2push="dir1/file1 dir2/file2" ${ant.file} push-files`

Push local files specifid by the 'sf.files2push' and 'sf.files2remove' properties to dependent org. Will delete metadata.
Can also specify files on the command-line.
- `ant -f ${ant.file} file-destructive-push`
- `ant -Dsf.files2remove="dir1/file1 dir2/file2" -Dsf.files2push="dir3/file3 dir4/file4" ${ant.file} file-destructive-push`

Push to dependent org from master org.
- `ant -f ${ant.file} push`

Destructive push from master org to dependent org.
- `ant -f ${ant.file} destructive-push`

### Pull from Salesforce org ###

Pull all metadata from a dependent org. *(folder based)*
- `ant -f ${ant.file} pull-full`

Pull all metadata to "master". *(folder based)*
- `ant -f ${ant.file} pull-full-to-master`

Pull from a dependent org. *(non-folder based)*
- `ant -f ${ant.file} pull`

Pull from a dependent org. *(non-folder based)*
- `ant -f ${ant.file} pull-to-master`

### Salesforce org analysis ###

Show differences between master org and dependent org.
- `ant -f ${ant.file} report-diff`

Generate CSV files for objects in the master org.
- `ant -f ${ant.file} generate-objects-csv`

Generate a CSV file of object diffs between master org and dependent org.
- `ant -f ${ant.file} generate-objects-diff-csv`

Describe all metadata.
- `ant -f ${ant.file} describe-metadata`

List metadata types specified by the 'sf.metadataTypes' property.
- `ant -Dsf.metadataTypes="metadata type(s)" -f ${ant.file} list-metadata`

Run all tests in a dependent org without deploying.
- `ant -f ${ant.file} run-tests`

### Manage multiple orgs ###

Display all known orgs.
- `ant -f ${ant.file} envs`

Verify specified credentials **TBD**
- `ant -f ${ant.file} verify-credentials`

Verify all credentials **TBD**
- `ant -f ${ant.file} verify-all-credentials`

List all ant targets.
- `ant -f ${ant.file} -projecthelp`


## Dependencies ##
+ Python
+ Ant
+ Python Beatbox (optional)

## Installation ##
### RPM ###
If you are on an RPM based system, you can run `make rpm` in the root directory and install the generated RPM
### SOURCE ###
1. Checkout the repo or extract the zip/tar
2. Symlink (or move) the repo to /usr/share/solenopsis/   `ln -s /path/to/checkout /usr/share/solenopsis/`
3. Symlink the solenopsis script into a bin directory   `ln -s /usr/share/solenopsis/scripts/solenosis /usr/local/bin/solenopsis`

## Setup ##
1. Get the ant library from your salesforce instance. Click Your Name | Setup | Develop | Tools, then flick Force.com Migration Tool
2. Extract the zip file and place the _ant-salesforce.jar_ in _/usr/share/solenopsis/ant/lib_
3. Make a directory for where your source will be checked out `mkdir ~/sfdc` and use this in the next step
4. Fetch your current sandbox and run `solenopsis pull-full-to-master`

## Workflows ##

### Basic Workflow ###

1. Get code from Salesforce org: `solenopsis pull-full`
2. Edit code
3. Push changes to Salesforce org: `solenopsis push`

### Team Workflow with Source Control ###

This workflow will keep your git repo clean and will make your life a lot easier for teams with multiple users.  We recommend that each developer have their own developer sandbox that they do their development on.  Then the code is pushed to a central full sandbox for testing.  This pushing can be done via [Jenkins](http://jenkins-ci.org/) with the aid of Solenopsis.

1. Get the most recent code from the git repo `git pull`
2. Push this code to your sandbox `solenopsis destructive-push`
3. Create a private feature branch `git checkout -b mynewfeature`
4. Do work locally and push `solenopsis push` (the command `solenopsis git-push` can be used for faster deployment of apex/VF code
5. Fetch work that was done in config from salesforce `solenopsis pull-full-to-master`
6. Commit work to private feature branch `git commit -a -m "My feature"`
7. See if there are any updates in the master branch `git checkout master` `git pull`
8. If there are changes since the last time you pulled, rebase and resolve issues `git checkout mynewfeature` `git rebase master`
9. After changes are resolved merge your code into master `git checkout master` `git merge --squash mynewfeature`
10. Commit your changes `git commit`
11. Push changes to master `git push`
12. Delete your feature branch `git branch -D mynewfeature`

## Caveats ##
Currently any folder based operations (email) must be created by hand (a blank file will do) in order to be able to fetch them.

### Fetch Email Template Example ###
We are currently working on a better way to do this

_This is all relative to your SFDC src directory_

1. Make the email directory `mkdir email`
2. Make the folder the email is in (this name is determined by SFDC, just look at the API name) `mkdir email/Case_Templates_internal`
3. Make the metadata file `touch email/Case_Templates_internal-meta.xml`
4. Make one of the emails in the folder (again the API name) `touch email/Case_Templates_internal/Case_Closed_internal.email`
5. Make the metadata file for it `touch email/Case_Templates_internal/Case_Closed_internal.email-meta.xml`
6. Pull the data from salesforce `solenopsis pull-full-to-master`

_For unfiled emails make the folder `mkdir email/unfiled\$public`_

_NOTE: Currently this project has only been developed and tested on Linux.  Patches and help for other platforms are always accepted._
