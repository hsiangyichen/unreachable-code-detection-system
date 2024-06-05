# Cpsc 410 Unreachable Code Checker Project Documentation

## Purpose and Goal

Analysis of unreachable code helps to reduce code complexity and confusion for users reading
code they might not be familiar with. By using our program. A user can identify which lines of code are 
unreachable, based on the control flow of the code.

## Specifications of our goal
Below are specifications of what we hope to be able to determine

1. determine that any statement under a return statement that is not within a branch is unreachable
2. determine whether a particular branch can occur based on control flow values


## Specifications of our analysis

1. we will only be handling comparisons between variables and integers
2. the conditionals for branching cases will all be binary comparisons (ex. a>y, or 1>2, or a> 2)
   1. the right side of conditional must have a fixed value if it is a variable (ex. x> y, x can be unbounded by y must have an assigned value)
   2. no arithmetics in conditional (ex a+1>y)
3. no functions call in other functions
4. no complicated updating functions beyond plus and minus
5. cannot update the bound in the loop

## Challenges

While it is trivial to determine that any statement after a return statement is unreachable, we would also 
like to determine statically whether certain branches are also unreachable, and record them in our final results as well if they are.

Reasons this is difficult

1. In static analysis, function parameters initially can have any value, so hard to exactly determine whether a branch would occur
2. Difficult to determine how many times or whether a for/while loop occurs, particularly if the loop body changes value

### Overall TradeOff Choices

#### For Challenge 1

##### Solution for Challenge 1

1. we decided to use a range interval for the possible values a parameter can be, with the range being initially set between lower infinity (negative infinity)
and upper infinity (positive infinity)
2. when an assignment occurs to a variable, the lower and upper bound gets set to the assignment value (ex. int x = 5, sets x's lower and upper bound to 5)
3. when a conditional occurs (if, for, while), the lower or upper bound gets set based on the condition (ex. if (x>5) for the then statement, lower bound of x set to 6, for the
else statement, upper bound gets set to 5. After both statements, the min lower bound of x and max upper bound of x of the two branches get set as the new bounds)

##### Benefit
   1. this gives a pessimistic over-approximation, as we will try consider a range of all possible values based on the control flow and variable state,
 and only in the case where it is definitely not possible to reach a statement, will we add it to the unreachable map.
      1. this allows for only have two sides of the range to compare instead of possibly trying all possible values
      2. Can also support assignments, as if a variable is given a fixed value like 5, both the upper and lower bound get changed to 5
      3. gives a way to represent infinite options
##### Cons
   1. this sometimes fails a Impossible four property (Always say yes when answer should be yes), as sometimes the range taking the union of both
branches means that some code that might actually be unreachable might not get added as the union of the two ranges might have a higher domain for values
than what is possible.

#### For Challenge 2

##### Solution for Challenge 2

1. for loops, we compute a delta for the change in variable state before and after the loop to try and predict how many times a loop will run.
2. to reduce the complexity of modifications that could happen to the variables that affect the loop, only addition and subtraction are allowed.

##### Benefit
   1. can get a slightly more accurate reading of how many times a loop will run compared to setting a particular N for how many times to unbound the loop to get
a pessimistic estimate

##### Cons
   1. restricted only for cases where the loop variable is modified via addition or subtraction, as complexity for all other cases causes
a large increase in runtime and the computed delta will become gradually less accurate and complex.

## Proposed Solution (1 and 2 summarized together)

Using upper and lower bound intervals for variable state and recording control flow

when the value of these variables are completely unknown, the range goes from neg inf to pos inf.

## State, Analysis, Concretization

### State

a map 'globalUnreachableMap' declared as:

```
Map<String, Set<String>> globalUnreachableMap = new HashMap<>();
```

a map 'unreachableMap' declared as:

```
Map<Integer, String> unreachableMap = new HashMap<>();
```

a map 'varMapLocal' declared as:

```
Map<String, VariableRange> varMapLocal = new HashMap<>();
```

### Concretization Function

**map 'globalUnreachableMap':**

1. key String which represents the signature of a function
2. value Set<String>, representing a set of String statements (with line number) that are unreachable in the function


map globalUnreachableMap is a collection of all the functions and all of their unreachable code

**map 'unreachableMap':** 

1. key Integer which represents the line number of the dead code
2. value String, which represents the line statement corresponding to the line number


map unreachableMap is a collection of unreachable lines and statements for a single function

**map 'varMapLocal':**

1. key String which represents a variable symbol
2. value variable range which contains:
   - field int 'lowerbound' which is the lower bound the variable could take for an int value
   - field int 'upperbound' which is the upper bound the variable could take for an int value
   - field boolean 'infUpperbound' true if there is no defined upper bound, false otherwise
   - field boolean 'infLowerbound' true if there is no defined lower bound, false otherwise


### Analysis Function

1. On non-assignment and non-conditional check, states do not change
2. On return statement
   1. all lines of code below the return statement belonging to a particular block get added to map unreachableMap
3. On conditional check While, For, If, if the interval of a variable being compared determines that only one branch is possible
   1. the not possible branch gets added to unreachableMap (Control flow sensitive)
   2. The other branch is evaluated
4. On conditional check While, For, If, if the interval of a variable being compared determines that both branch is possible
   1. Clone current state of variableRange (for each branch)
   2. modify the range of the variable used in the conditional based on the conditional (ex. if (x> 5), one branch puts x lower bound at 6, other branch puts upper bound at 5)
   3. proceed through both branches normally
   4. once both branches are analyzed, create new variable Range where the lower bound is the min of the two lower bounds and upper bound is the max of the two upper bounds
5. once entire function is evaluated,
   1. create new map for 'globalUnreachableMap' (Map<String, Set<String>>)
   2. Create new set 'tmp' (Set<String>)
   3. for each key,val pair in 'unreachableMap', combine line number and statement into 1 string and add to set 'tmp'
   4. add 'tmp' to globalUnreachableMap with key as the function Signature the set 'tmp' belongs to.







