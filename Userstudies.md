## First User Studies

- It is based on the feedback of the survey.
- Survey link: https://docs.google.com/document/d/10j_7blwcfZqkzG-1jhLBZdQ4SH68FNVKhnZ9FCu9sr0/edit

### First participant (Computer Science Grads Student):

1.  Not able to identify redundant code correctly with human eye.
2.  Lack of accuracy in identifying redundant code despite 5 years of Java experience.
3.  Suggested improvement: Clearly indicate unnecessary code lines and explain why they won't execute.

### Second participant (Computer Science third-year Student):

1.  Advocates for static code analysis and refactoring suggestions.
2.  Proposes a detailed dashboard for code health metrics.
3.  Recommends ML algorithms for better code optimization and sharing analysis among development teams.

### Third participant (Computer Science Student):

1.  Not able to identify redundant code correctly with human eye.
2.  Highlight the line/block and determine if the line is needed or not is recommended.
3.  If it is not ide integrated would be fine.

### Main Points that We Can Focus On for Our Initial Project

1. **Automated Clean-up:**

   - Enable automatic removal of redundant code segments or offer optimization suggestions for cleaner code maintenance.

2. **Detailed Dashboard & Metrics:**

   - Develop comprehensive dashboards showing code health metrics like duplication and dependencies for analysis.

3. **User-Friendly Interfaces:**

   - Prioritize intuitive interfaces aiding developers in understanding code defects and suggested optimizations.

4. **Machine Learning Integration:**

   - Explore ML algorithms to enhance accuracy in identifying redundant or unreachable code segments effectively.

5. **Error Sharing & Collaboration:**
   - Facilitate sharing of errors and analysis results among development teams for collaborative issue resolution.

## Second User Studies

- It is based on the feedback of the interview.

### First Participant (Senior Developer):

1. The tool missed some instances of unreachable code, especially in conditional branches and after exception throws.
   Accuracy in these areas is crucial for my daily tasks.
2. It would be helpful if the tool could provide suggestions on why certain code is unreachable and how to address it.
3. It would be great to allow more than one file/classes at a time.
4. It's a good standalone tool, but having plugins for popular IDEs or version control systems could streamline
   the workflow further.
5. Despite some misses, it's a great tool that raises awareness about code quality. With further accuracy improvements,
   I'd highly recommend it.

### Second Participant (Computer Science Grads Student):

1. It successfully identified straightforward cases of unreachable code, but it seems to struggle with more complex
   control flow scenarios. Enhancing its ability to navigate and analyze these would make it invaluable.
2. I appreciate the simplicity, but a feature to filter or sort results by type of unreachable code or severity could
   help manage larger projects since it is not in order now.
3. It shows promise, especially for educational purposes or smaller projects. For enterprise use, enhancing its
   detection capabilities is key.
4. Visual diffs that highlight unreachable code before and after changes in the codebase would be a powerful feature
   for ensuring code quality, especially for new users.

### Main Points that We Need to Enhance

1. **Accuracy**
   - Improve detection in complex scenarios, especially conditional branches and after exception throws.
2. **Actionable Insights**
   - Provide reasons for unreachable code and suggestions for remediation.
3. **Integration**
   - Enhance plugin support for popular IDEs and version control systems.
4. **Filtering/Sorting**
   - Add features to prioritize issues by type or severity.
5. **Visualization**
   - Implement visual diffs for clearer before-and-after comparisons.

## Final User Studies

- It is based on the feedback of the interview.

1. **Accuracy and Effectiveness**
   - Having a broader analysis scope beyond variable-integer comparisons to include complex data types and expressions would better serve real-world, diverse coding scenarios.
   - Not able to detect try catch exceptions.
2. **Interface Improvement**
   - Having a whole file with red background color to show users the unreachable code will be better than only showing the lines and the corresponding code.
3. **Function Call Restrictions**
   - Users are able to view unreachable code with any kinds of intercalls is recommended since most java project had a lot of dependency from function and classes.
4. **Classes Restrictions**
   - Unable to detect multiple classes at a time.
