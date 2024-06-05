"use client";

import Link from "next/link";

const Page = () => {
  return (
    <div className="">
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-2xl my-4 font-normal mb-2">About Us</h1>
        <p className="text-md mb-6">
          We are dedicated to improving code quality and efficiency through
          innovative static analysis tools.
        </p>
        <div className=" flex flex-row gap-10">
          <div className="bg-white shadow-xl h-auto basis-1/2 p-10 duration-300 hover:translate-y-2">
            <h2 className="text-xl font-semibold mb-2">Our Mission</h2>
            <p className="text-lg">
              Our mission is to empower developers with tools that simplify code
              analysis and facilitate the enhancement of software quality.
            </p>
          </div>
          <div className="bg-white shadow-xl h-auto basis-1/2 p-10 duration-300 hover:translate-y-2">
            <h2 className="text-xl font-semibold mb-2">What We Offer</h2>
            <p className="text-lg">
              We offer a static analysis tool specifically designed to detect
              unreachable code segments in Java programs. Our tool provides
              actionable insights to improve code quality and efficiency.
            </p>
          </div>
        </div>
        <h1 className="text-2xl font-normal mb-2 mt-10">Our Project</h1>
        <p className="text-md mb-6">
          Here, we outlines the details of our project and the methods we used
          to identify unreachable code, emphasizing static analysis, variable
          state ranges, and control flow.
        </p>
        <div className="flex flex-col gap-10">
          <div className="bg-white shadow-xl h-auto p-10 duration-300 hover:translate-y-2">
            <h2 className="text-xl font-semibold mb-2">
              Our Main Focus of the Methods
            </h2>
            <ul className="list-disc pl-5">
              <li>
                Determine if any statement under a return statement, not within
                a branch, is unreachable.
              </li>
              <li>
                Assess the possibility of a particular branch based on control
                flow values.
              </li>
            </ul>
          </div>

          <div className="bg-white shadow-xl h-auto p-10 duration-300 hover:translate-y-2">
            <h2 className="text-xl font-semibold mb-2">Our Analysis</h2>
            <ul className="list-disc pl-5">
              <div>
                1. Handling only comparisons between variables and integers.
              </div>
              <div>
                {`2. All conditionals for branching cases will be binary comparisons,
                with specific conditions (ex. a>y, or 1>2, or a> 2).`}
              </div>
              <p>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                {`a. The right side of conditional must have a fixed value if it is a variable. `}
                <br />
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                {`- ex: x> y, x can be unbounded by y must have an assigned value`}
              </p>
              <p>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                {`b. No arithmetics in conditional `}
                <br />
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                {`- ex: a+1>y`}
              </p>
              <div>
                3. No functions call within other functions, and restricted
                updating functions to plus and minus operations.
              </div>
              <div>4. Cannot update the bound in the loop.</div>
            </ul>
          </div>

          <div className="bg-white shadow-xl h-auto p-10 duration-300 hover:translate-y-2">
            <h2 className="text-xl font-semibold mb-2">Challenges</h2>
            <div>
              <p>There are 2 reasons why this is challenging:</p>
              <ul className="list-disc pl-5">
                <li>
                  In static analysis, function parameters initially can have any
                  value, so hard to exactly determine whether a branch would
                  occur
                </li>
                <li>
                  Difficult to determine how many times or whether a for/while
                  loop occurs, particularly if the loop body changes value
                </li>
              </ul>
            </div>
            <h2 className="text-xl font-semibold mt-6 mb-4">
              Overall Trade-Off Choices
            </h2>

            {/* Section for Challenge 1 */}
            <div className="">
              <h2 className="text-lg font-semibold">- Challenge 1</h2>
              <div className="flex flex-row gap-10 m-5">
                <div className="basis-1/3">
                  <h3 className="text-md font-medium">Solution:</h3>
                  <div className="p-3  border-t-[1.5px] border-zinc-200">
                    <li className="p-2">
                      Use of a range interval for parameter values, starting
                      from negative to positive infinity.
                    </li>
                    <li className="p-2">
                      Assignment to a variable sets its bounds to the assignment
                      value.
                    </li>
                    <li className="p-2">
                      Conditional statements adjust bounds based on the
                      condition.
                    </li>
                  </div>
                </div>
                <div className="basis-1/3">
                  <h3 className="text-md font-medium">Benefits:</h3>
                  <p className="p-3 border-t-[1.5px] border-zinc-200">
                    Provides a pessimistic over-approximation, supports fixed
                    value assignments, and represents infinite options.
                  </p>
                </div>
                <div className="basis-1/3">
                  <h3 className="text-md font-medium">Cons:</h3>
                  <p className="p-3  border-t-[1.5px] border-zinc-200">
                    May fail the Impossible four property, as union of ranges
                    might overestimate the possible value domain.
                  </p>
                </div>
              </div>
            </div>

            {/* Section for Challenge 2 */}

            <div className="mt-4">
              <h2 className="text-lg font-semibold">- Challenge 2</h2>
              <div className="flex flex-row gap-10 m-5">
                <div className="basis-1/3">
                  <h3 className="text-md font-medium">Solution:</h3>
                  <p className="p-3 border-t-[1.5px] border-zinc-200">
                    Computes a delta for variable state change in loops,
                    supporting only addition and subtraction for modification.
                  </p>
                </div>
                <div className="basis-1/3">
                  <h3 className="text-md font-medium">Benefits:</h3>
                  <p className="p-3 border-t-[1.5px] border-zinc-200">
                    Offers a more accurate loop run estimate compared to static
                    unrolling.
                  </p>
                </div>
                <div className="basis-1/3">
                  <h3 className="text-md font-medium">Cons:</h3>
                  <p className="p-3  border-t-[1.5px] border-zinc-200">
                    Applicable only to loops with variables modified by addition
                    or subtraction.
                  </p>
                </div>
              </div>
            </div>
            {/* Section for Proposed Solution and State Analysis */}
            <div className="my-6">
              <h2 className="text-xl font-semibold">
                Proposed Solution and State Analysis
              </h2>
              <p>
                By using upper and lower bound intervals for variable states and
                careful control flow recording, our approach aims to accurately
                identify unreachable code under specified constraints.
              </p>
            </div>
          </div>

          <div className="bg-white shadow-xl h-auto p-10 duration-300 hover:translate-y-2">
            <h2 className="text-xl font-semibold mb-2">
              State, Analysis, Concretization
            </h2>
            <p>
              The analysis leverages maps to track unreachable code, variable
              states, and the relations between them, employing a comprehensive
              strategy to navigate and analyze the complexities of code flow.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Page;
