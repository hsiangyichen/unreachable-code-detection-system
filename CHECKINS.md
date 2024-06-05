# CHECKIN 1

## Followup Question:

Want to get an idea of how far of a scope the project should have (a static analysis is required but is it sufficient for example)

### Idea 1: Java Function Lifecycle Visual Display

#### Proposal:

1. Given an arbitrary Java problem, statically analyze the AST with a tool (Spoon) and generate a tree display of the classes, functions, variables, and other classes defined within them.
2. Additionally, using a tool like PMD, display some code smells, checkstyles, bugs that can be identified statically from the implementations.
3. Create a visual representation of the lifecycle of the Java program, potentially depicting the classes used and the functions that may get called, either directly or through branching.

#### Possible Distribution:

- 1-2 group members work on utilizing and modifying the Spoon AST for use.
- 1-2 group members implement the code for analyzing and modifying the AST data generated to perform the desired tasks.
- 1-2 group members decide on which visual library to use for the display.

### Idea 2: Java Dependency Helper

#### Proposal:

1. Given an arbitrary Java problem, statically analyze the AST with a tool (Spoon) and generate a UML display of the classes, functions, variables, and other classes defined within them. Additionally, generate arrows and other symbols to show relationships between the classes.

#### Possible Distribution (Same as above):

- 1-2 group members work on utilizing and modifying the Spoon AST for use.
- 1-2 group members implement the code for analyzing and modifying the AST data generated to perform the desired tasks.
- 1-2 group members decide on which visual library to use for the display.

# CHECKIN 2

## Project Overview

Our project aims to detect unnecessary code (either redundant or unreachable) in Java through static analysis and provide an interactive visualization.

## Program Analysis Pipeline

The program analysis pipeline should be as follows:

1. **AST Conversion**

   - Using the Spoon library, we will convert the given Java code into an AST representation and use Spoon's API to walk through and analyze each node in the AST.

2. **Redundant Code Analysis**

   - Utilizing the AST, we identify duplicate or similar code blocks within methods and classes.
   - Analyze variables that do not contribute to the code's functionality.

3. **Unreachable Code Analysis**

   - Detecting code that can never be executed due to conditional statements or method calls.
   - Checking for unreachable code blocks (unreachable catch blocks, unreachable return calls, and unreachable code blocks under return statements).

4. **Visualization Ideas**
   - **Code Heatmaps:** Visualize code blocks based on their redundancy and reachability (perhaps through the frequency of execution). This could be represented using color gradients.
   - **Reporting: Redundancy Diff Checkers:** Provide several diff checkers for each redundant code, allowing users to identify similar code blocks.
   - **Code Flow Diagrams:** Show the code flow diagram, indicating parts of the code that are called at a stage and code (or branches) that are never called.

## Follow-Up Designs

We still need to specify in more detail the set of code that can be detected using this program analysis, determining the complexity of the analysis, and thus how to analyze more complex cases of code.

## Planned Division of Tasks

- **Frontend:**
  - Michelle C and Deo will focus on frontend development.
- **Backend and Integration:**
  - Michelle T, Hawk, and Victor will focus on backend development and working with the frontend team to integrate.

## Summary of Progress So Far

We finished up the idea and went to office hours to gain a better understanding of the scope.

## Roadmap

- **This Week:** Finish off our idea.
- **Next Week (March 11-18):** Determine the technical structure.
- **March 18-25:** Develop the MVP code.
- **March 25-April 1:** Continue coding.
- **April 1-8:** Test if needed.

# CHECKIN 3 (modifications from Checkin 2)

Our project aims to detect unnecessary code (either redundant or unreachable) in Java through static analysis and provide an interactive visualization. The program analysis pipeline should be as follows:

1. **AST Conversion**
   Using the Spoon library, we will convert the given Java code into an AST representation and use Spoon's API to walk through and analyze each node in the AST.

2. **Unreachable code analysis (Static Analysis)**
   Detecting code that can never be executed due to conditional statements or method calls.
   Checking for unreachable code blocks (unreachable catch blocks, unreachable return calls, and unreachable code blocks under return statements).

3. **UI Design Frontend**
   We will have a frontend component for a user to input a Java file (could be upload or drag). This will send the Java file to our backend, which will compute the necessary information and display the results on the frontend in a table-like form.

4. **Java Backend**
   We will have a Java server component, which will receive a Java file from the frontend, run Spoon to generate the AST, analyze the AST for unreachable code, record it in state, and return the results to the frontend.

5. **Info to report, prospectively**:

   - The actual code that is unreachable.
   - The line number of unreachable code.
   - A reasoning on why the code is unreachable.
   - % of code that is reachable, % that is unreachable.

6. **User Study**
   The user study is going to be conducted over the weekend, with an emphasis on examples in which the tool could be used and determining if the user is able to detect the unreachable code and if they would desire a tool to do this automatically.

### Planned division of task

- One person will primarily work on the frontend.

- Everyone else will work on the backend, including setting up the server, utilizing Spoon to generate the AST, analyzing the AST, and recording state.

### Roadmap

- March 18-25: MVP Code
- March 25-April 1: Code and code
- April 1-8: Test if needed

### Questions for User Study

- **Initial Impressions**

  - After reviewing the project proposal, what are your initial thoughts or impressions?
  - Which aspect of the proposed tool (static analysis, visualization, both) do you find most valuable for your work?

- **Understanding of Problem Statement**

  - Do you believe that detecting unnecessary code in Java (such as redundant or unreachable code) is an important task for software development?
  - How do you currently identify unnecessary code in your Java projects?

- **Expected Features**

  - What specific features or functionalities would you expect from a tool designed to detect unnecessary code in Java?
  - Are there any additional analysis or visualization features you would like to see in such a tool?

- **Usability and Visualizations**
  - How important is user interface (UI) design and ease of use for a program analysis tool like the one proposed?
  - What types of visualizations do you think would be most helpful for understanding code structure and dependencies?

# CHECKIN 4

## Status of Implementation So Far

- Frontend: Next.js project has been set up
- Backend: The Java server component has been set up and Spoon library integration for AST generation has been completed.

## Plans for Final User Study

- Conducting the Study:
  - Participants will be provided with sample Java files containing various instances of unnecessary code.
  - They will be asked to use our tool to identify and understand the detected unnecessary code.
  - We will observe their interactions and gather feedback through interview and simple observation.
- Analyzing Results:
  - We will analyze the feedback received to identify anything that we can improve in our tool.

## Planned Timeline for the Remaining Days

- March 25-April 1:
  - Implemented the visiter for each cases.
  - Implemented static analysis to detect unreachable code within the Java files.
  - Preparation of sample Java files and study materials for the final user study.
- April 1-8:
  - Conducting the final user study.
  - Analysis of study results and refinement of the tool based on feedback.
  - Create your video and ready for project submission.

# CHECKIN 5

## Final user Study
 
- User study not yet done as the MVP code is not yet finished
- Planned to be done next week

## Plan for video 

## Timeline

- By April 1st: Get MVP code done
- April 2nd - 8th: Do user study and video. Debug as bugs are found

## Progress against checkin 2 timeline

- MVP code is a bit behind expectations.
- Other than that, project seems to be following along the timeline 
