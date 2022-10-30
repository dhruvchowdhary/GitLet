# Gitlet Design Document
author: DHruv Chowdhary

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

### Main.java
This class is the entry point of the program. It implements methods to
set up persistence and support each command of the program.

#### Fields
1. static final File CWD: A pointer to the current working directory of
the program
2. static final File GITLET: A pointer to the .gitlet directory in the
current working directory

### Blob.java
This class represents a blob instance.

#### Fields
1. File file: This is the file we wish to commit
2. String content: Has the content of the file we want to commit

### Stage.java
This class represent the staging area of what files to be added, changed,
or removed in the commit.

#### Fields
1. ArrayList<File> addition: Array list of files that are staged for
addition
2. ArrayList<File> removal: Array list of files that are staged for
   removal

### Commit.java
This class represents a commit instance.

#### Fields
1. String parent: A string of the previous commit instance's uid.
2. String message: Contains the message passed in for the commit
3. String timestamp: Contains the timestamp of the commit
4. String metadata: Contains the metadata of the commit- message and timestamp
5. String uid: Unique identifier for the commit hashcoded
6. Boolean isMaster: Returns if the commit is the master
7. Boolean isHead: Returns if the commit is HEAD
8. ArrayList<Blob> blob: Contains all the blobs of the files we are committing

### Branch.java
This class represents a branch instance.

#### Fields
1. ArrayList<String> commits: An arraylist of the uid's of the commits
in this branch.

## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

### Main.java
1. main(String[] args): This is the entry point of the program. It would contain
the if statements checking which command is to be called and will then call that
method.
2. init(): creates the .gitlet repository in the current directory by calling setupPersistance(). It creates the initial commit with message "initial commit" and the timestamp. Makes the Master and HEAD pointer to the initial commit. Initialize the staging area.
3. add(Array<String>): Puts the files in the staging area.
4. commit(): Creates a new commit by cloning the parent. Changes the metadata with new message and timestamp. Uses staging to reflect the changes and clears it. Moves the Master and HEAD pointer.
5. rm(): Will work on after checkpoint
6. log(): Goes from head commit to parent commit using the parent's uid displaying the hash, date, and commit message until the parent is null.
7. global-log(): Will work on after checkpoint
8. find(): Will work on after checkpoint
9. status(): Will work on after checkpoint
10. checkout(): Replaces the file with either the HEAD commit version of the file or the version of a specific commit using id.
11. branch(): Will work on after checkpoint
12. rm-branch(): Will work on after checkpoint
13. reset(): Will work on after checkpoint
14. merge(): Will work on after checkpoint

## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

### commit [all instance variables]
- After we commit, we save the commit in a text file named after its uid saved in the commits folder inside .gitlet.

### blob [file] [content]
- After each commit, we save the blob in a text file named after the file it is saving with the content of the file inside. This is saved in the blobs folder inside .gitlet.

### stage [addition][removal]
- After each addition or removal, we save the file names inside either a text file named addition or removal which are both in the stage folder inside .gitlet.

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

