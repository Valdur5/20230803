# Requirements
* Java 14+ (for using records)

# Functional Description

* Task 1-5 will need an input array defining which nodes to visit. As in the examples given the paths are not always the shortest paths and there are more combination possible the only way to achive the required result is to totally define the paths for those 5 use cases.
* Task 6+7 will need the start and end node and the max amount of hops as an input.
* Task 8+9 will need the start and end node as input.
* Task 10 will need a start and end node and the max latency as input.

## In total, we can categorize the functions in 4 broad categories:

1) Calculate average based on a given path - trivial based on our tree node structure with a lookup map.
2) Shortest Path calculation with BFS limiting by max hops between nodes with multiple outcomes.
3) Shortest Path calculation with BFS only returning the lowest latency path.
4) 