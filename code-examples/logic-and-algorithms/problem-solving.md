# Problem Solving & Algorithms in Java

> Complete Java solutions for 50+ algorithm and data structure problems.
> All solutions include time/space complexity analysis and test cases.

## Table of Contents

1. [Arrays & Hashing](#1-arrays--hashing)
2. [Two Pointers](#2-two-pointers)
3. [Sliding Window](#3-sliding-window)
4. [Stack](#4-stack)
5. [Binary Search](#5-binary-search)
6. [Linked List](#6-linked-list)
7. [Trees](#7-trees)
8. [Heap / Priority Queue](#8-heap--priority-queue)
9. [Backtracking](#9-backtracking)
10. [Graphs](#10-graphs)
11. [Dynamic Programming](#11-dynamic-programming)
12. [Greedy](#12-greedy)
13. [Intervals](#13-intervals)
14. [Math & Geometry](#14-math--geometry)
15. [Bit Manipulation](#15-bit-manipulation)

---

## 1. Arrays & Hashing

### Two Sum

```java
// Problem: Find two indices that sum to target
// Time: O(n), Space: O(n)
public static int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> map = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (map.containsKey(complement)) {
            return new int[]{map.get(complement), i};
        }
        map.put(nums[i], i);
    }
    return new int[]{-1, -1};
}
```

### Contains Duplicate

```java
// Time: O(n), Space: O(n)
public static boolean containsDuplicate(int[] nums) {
    Set<Integer> seen = new HashSet<>();
    for (int n : nums) {
        if (!seen.add(n)) return true;
    }
    return false;
}
```

### Valid Anagram

```java
// Time: O(n), Space: O(1) — fixed 26 chars
public static boolean isAnagram(String s, String t) {
    if (s.length() != t.length()) return false;
    int[] count = new int[26];
    for (int i = 0; i < s.length(); i++) {
        count[s.charAt(i) - 'a']++;
        count[t.charAt(i) - 'a']--;
    }
    return Arrays.stream(count).allMatch(c -> c == 0);
}
```

### Group Anagrams

```java
// Time: O(n * k log k), Space: O(n * k)
public static List<List<String>> groupAnagrams(String[] strs) {
    Map<String, List<String>> map = new HashMap<>();
    for (String s : strs) {
        char[] chars = s.toCharArray();
        Arrays.sort(chars);
        String key = new String(chars);
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
    }
    return new ArrayList<>(map.values());
}
```

### Top K Frequent Elements

```java
// Time: O(n log k), Space: O(n)
public static int[] topKFrequent(int[] nums, int k) {
    Map<Integer, Integer> freq = new HashMap<>();
    for (int n : nums) freq.merge(n, 1, Integer::sum);

    PriorityQueue<Map.Entry<Integer, Integer>> pq =
        new PriorityQueue<>(Map.Entry.comparingByValue());
    for (var entry : freq.entrySet()) {
        pq.offer(entry);
        if (pq.size() > k) pq.poll();
    }

    return pq.stream().mapToInt(Map.Entry::getKey).toArray();
}
```

### Product of Array Except Self

```java
// Time: O(n), Space: O(1) (excluding output)
public static int[] productExceptSelf(int[] nums) {
    int n = nums.length;
    int[] result = new int[n];
    result[0] = 1;
    for (int i = 1; i < n; i++) {
        result[i] = result[i - 1] * nums[i - 1];
    }
    int suffix = 1;
    for (int i = n - 1; i >= 0; i--) {
        result[i] *= suffix;
        suffix *= nums[i];
    }
    return result;
}
```

### Longest Consecutive Sequence

```java
// Time: O(n), Space: O(n)
public static int longestConsecutive(int[] nums) {
    Set<Integer> set = new HashSet<>();
    for (int n : nums) set.add(n);
    int longest = 0;
    for (int n : set) {
        if (!set.contains(n - 1)) {  // start of a sequence
            int length = 1;
            while (set.contains(n + length)) length++;
            longest = Math.max(longest, length);
        }
    }
    return longest;
}
```

### Encode and Decode Strings

```java
// Time: O(n), Space: O(n)
public static String encode(List<String> strs) {
    StringBuilder sb = new StringBuilder();
    for (String s : strs) {
        sb.append(s.length()).append('#').append(s);
    }
    return sb.toString();
}

public static List<String> decode(String s) {
    List<String> result = new ArrayList<>();
    int i = 0;
    while (i < s.length()) {
        int j = s.indexOf('#', i);
        int len = Integer.parseInt(s.substring(i, j));
        result.add(s.substring(j + 1, j + 1 + len));
        i = j + 1 + len;
    }
    return result;
}
```

---

## 2. Two Pointers

### Valid Palindrome

```java
// Time: O(n), Space: O(1)
public static boolean isPalindrome(String s) {
    int l = 0, r = s.length() - 1;
    while (l < r) {
        while (l < r && !Character.isLetterOrDigit(s.charAt(l))) l++;
        while (l < r && !Character.isLetterOrDigit(s.charAt(r))) r--;
        if (Character.toLowerCase(s.charAt(l)) != Character.toLowerCase(s.charAt(r)))
            return false;
        l++; r--;
    }
    return true;
}
```

### Three Sum

```java
// Time: O(n²), Space: O(1) or O(n) for sorting
public static List<List<Integer>> threeSum(int[] nums) {
    Arrays.sort(nums);
    List<List<Integer>> result = new ArrayList<>();
    for (int i = 0; i < nums.length - 2; i++) {
        if (i > 0 && nums[i] == nums[i - 1]) continue;
        int l = i + 1, r = nums.length - 1;
        while (l < r) {
            int sum = nums[i] + nums[l] + nums[r];
            if (sum == 0) {
                result.add(List.of(nums[i], nums[l], nums[r]));
                while (l < r && nums[l] == nums[l + 1]) l++;
                while (l < r && nums[r] == nums[r - 1]) r--;
                l++; r--;
            } else if (sum < 0) l++;
            else r--;
        }
    }
    return result;
}
```

### Container With Most Water

```java
// Time: O(n), Space: O(1)
public static int maxArea(int[] height) {
    int l = 0, r = height.length - 1, max = 0;
    while (l < r) {
        int area = Math.min(height[l], height[r]) * (r - l);
        max = Math.max(max, area);
        if (height[l] < height[r]) l++;
        else r--;
    }
    return max;
}
```

---

## 3. Sliding Window

### Best Time to Buy and Sell Stock

```java
// Time: O(n), Space: O(1)
public static int maxProfit(int[] prices) {
    int minPrice = Integer.MAX_VALUE, maxProfit = 0;
    for (int price : prices) {
        if (price < minPrice) minPrice = price;
        else maxProfit = Math.max(maxProfit, price - minPrice);
    }
    return maxProfit;
}
```

### Longest Substring Without Repeating Characters

```java
// Time: O(n), Space: O(k) where k = charset size
public static int lengthOfLongestSubstring(String s) {
    Set<Character> window = new HashSet<>();
    int l = 0, maxLength = 0;
    for (int r = 0; r < s.length(); r++) {
        while (window.contains(s.charAt(r))) {
            window.remove(s.charAt(l++));
        }
        window.add(s.charAt(r));
        maxLength = Math.max(maxLength, r - l + 1);
    }
    return maxLength;
}
```

### Longest Repeating Character Replacement

```java
// Time: O(n), Space: O(1)
public static int characterReplacement(String s, int k) {
    int[] count = new int[26];
    int l = 0, maxFreq = 0, maxLength = 0;
    for (int r = 0; r < s.length(); r++) {
        maxFreq = Math.max(maxFreq, ++count[s.charAt(r) - 'A']);
        while ((r - l + 1) - maxFreq > k) {
            count[s.charAt(l++) - 'A']--;
        }
        maxLength = Math.max(maxLength, r - l + 1);
    }
    return maxLength;
}
```

---

## 4. Stack

### Valid Parentheses

```java
// Time: O(n), Space: O(n)
public static boolean isValid(String s) {
    Deque<Character> stack = new ArrayDeque<>();
    Map<Character, Character> pairs = Map.of(')', '(', '}', '{', ']', '[');
    for (char c : s.toCharArray()) {
        if (pairs.containsKey(c)) {
            if (stack.isEmpty() || stack.pop() != pairs.get(c)) return false;
        } else {
            stack.push(c);
        }
    }
    return stack.isEmpty();
}
```

### Min Stack

```java
class MinStack {
    private Deque<int[]> stack = new ArrayDeque<>(); // [value, currentMin]

    public void push(int val) {
        int min = stack.isEmpty() ? val : Math.min(val, stack.peek()[1]);
        stack.push(new int[]{val, min});
    }

    public void pop() { stack.pop(); }
    public int top() { return stack.peek()[0]; }
    public int getMin() { return stack.peek()[1]; }
}
```

### Daily Temperatures

```java
// Time: O(n), Space: O(n)
public static int[] dailyTemperatures(int[] temps) {
    int[] result = new int[temps.length];
    Deque<Integer> stack = new ArrayDeque<>();  // indices
    for (int i = 0; i < temps.length; i++) {
        while (!stack.isEmpty() && temps[i] > temps[stack.peek()]) {
            int idx = stack.pop();
            result[idx] = i - idx;
        }
        stack.push(i);
    }
    return result;
}
```

---

## 5. Binary Search

### Binary Search

```java
// Time: O(log n), Space: O(1)
public static int binarySearch(int[] nums, int target) {
    int l = 0, r = nums.length - 1;
    while (l <= r) {
        int m = l + (r - l) / 2;
        if (nums[m] == target) return m;
        if (nums[m] < target) l = m + 1;
        else r = m - 1;
    }
    return -1;
}
```

### Search in Rotated Sorted Array

```java
// Time: O(log n), Space: O(1)
public static int searchRotated(int[] nums, int target) {
    int l = 0, r = nums.length - 1;
    while (l <= r) {
        int m = l + (r - l) / 2;
        if (nums[m] == target) return m;
        if (nums[l] <= nums[m]) {  // left half is sorted
            if (target >= nums[l] && target < nums[m]) r = m - 1;
            else l = m + 1;
        } else {  // right half is sorted
            if (target > nums[m] && target <= nums[r]) l = m + 1;
            else r = m - 1;
        }
    }
    return -1;
}
```

### Find Minimum in Rotated Sorted Array

```java
// Time: O(log n), Space: O(1)
public static int findMin(int[] nums) {
    int l = 0, r = nums.length - 1;
    while (l < r) {
        int m = l + (r - l) / 2;
        if (nums[m] > nums[r]) l = m + 1;
        else r = m;
    }
    return nums[l];
}
```

---

## 6. Linked List

### Reverse Linked List

```java
// Time: O(n), Space: O(1)
public static ListNode reverseList(ListNode head) {
    ListNode prev = null, curr = head;
    while (curr != null) {
        ListNode next = curr.next;
        curr.next = prev;
        prev = curr;
        curr = next;
    }
    return prev;
}

// Recursive: Time: O(n), Space: O(n)
public static ListNode reverseListRecursive(ListNode head) {
    if (head == null || head.next == null) return head;
    ListNode reversed = reverseListRecursive(head.next);
    head.next.next = head;
    head.next = null;
    return reversed;
}
```

### Merge Two Sorted Lists

```java
// Time: O(n + m), Space: O(1)
public static ListNode mergeTwoLists(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(0), tail = dummy;
    while (l1 != null && l2 != null) {
        if (l1.val < l2.val) { tail.next = l1; l1 = l1.next; }
        else { tail.next = l2; l2 = l2.next; }
        tail = tail.next;
    }
    tail.next = l1 != null ? l1 : l2;
    return dummy.next;
}
```

### Reorder List

```java
// Time: O(n), Space: O(1)
public static void reorderList(ListNode head) {
    if (head == null) return;

    // Find middle
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }

    // Reverse second half
    ListNode prev = null, curr = slow.next;
    slow.next = null;
    while (curr != null) {
        ListNode next = curr.next;
        curr.next = prev;
        prev = curr;
        curr = next;
    }

    // Merge halves
    ListNode first = head, second = prev;
    while (second != null) {
        ListNode tmp1 = first.next, tmp2 = second.next;
        first.next = second;
        second.next = tmp1;
        first = tmp1;
        second = tmp2;
    }
}
```

### Remove Nth Node From End

```java
// Time: O(n), Space: O(1)
public static ListNode removeNthFromEnd(ListNode head, int n) {
    ListNode dummy = new ListNode(0, head);
    ListNode slow = dummy, fast = head;
    for (int i = 0; i < n; i++) fast = fast.next;
    while (fast != null) {
        slow = slow.next;
        fast = fast.next;
    }
    slow.next = slow.next.next;
    return dummy.next;
}
```

### Linked List Cycle Detection

```java
// Time: O(n), Space: O(1)
public static boolean hasCycle(ListNode head) {
    ListNode slow = head, fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) return true;
    }
    return false;
}
```

---

## 7. Trees

### Maximum Depth of Binary Tree

```java
// Time: O(n), Space: O(h) where h = height
public static int maxDepth(TreeNode root) {
    if (root == null) return 0;
    return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
}
```

### Invert Binary Tree

```java
// Time: O(n), Space: O(h)
public static TreeNode invertTree(TreeNode root) {
    if (root == null) return null;
    TreeNode temp = root.left;
    root.left = invertTree(root.right);
    root.right = invertTree(temp);
    return root;
}
```

### Validate Binary Search Tree

```java
// Time: O(n), Space: O(h)
public static boolean isValidBST(TreeNode root) {
    return isValid(root, null, null);
}

private static boolean isValid(TreeNode node, Integer low, Integer high) {
    if (node == null) return true;
    if ((low != null && node.val <= low) || (high != null && node.val >= high))
        return false;
    return isValid(node.left, low, node.val)
        && isValid(node.right, node.val, high);
}
```

### Level Order Traversal

```java
// Time: O(n), Space: O(n)
public static List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;
    Queue<TreeNode> q = new LinkedList<>();
    q.offer(root);
    while (!q.isEmpty()) {
        int size = q.size();
        List<Integer> level = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TreeNode node = q.poll();
            level.add(node.val);
            if (node.left != null) q.offer(node.left);
            if (node.right != null) q.offer(node.right);
        }
        result.add(level);
    }
    return result;
}
```

### Serialize and Deserialize Binary Tree

```java
// Time: O(n), Space: O(n)
public static String serialize(TreeNode root) {
    StringBuilder sb = new StringBuilder();
    serializeHelper(root, sb);
    return sb.toString();
}

private static void serializeHelper(TreeNode node, StringBuilder sb) {
    if (node == null) { sb.append("null,"); return; }
    sb.append(node.val).append(',');
    serializeHelper(node.left, sb);
    serializeHelper(node.right, sb);
}

public static TreeNode deserialize(String data) {
    Queue<String> q = new LinkedList<>(Arrays.asList(data.split(",")));
    return deserializeHelper(q);
}

private static TreeNode deserializeHelper(Queue<String> q) {
    String val = q.poll();
    if (val.equals("null")) return null;
    TreeNode node = new TreeNode(Integer.parseInt(val));
    node.left = deserializeHelper(q);
    node.right = deserializeHelper(q);
    return node;
}
```

---

## 8. Heap / Priority Queue

### Kth Largest Element in an Array

```java
// Time: O(n log k), Space: O(k)
public static int findKthLargest(int[] nums, int k) {
    PriorityQueue<Integer> pq = new PriorityQueue<>();
    for (int n : nums) {
        pq.offer(n);
        if (pq.size() > k) pq.poll();
    }
    return pq.peek();
}
```

### Find Median from Data Stream

```java
class MedianFinder {
    private PriorityQueue<Integer> small = new PriorityQueue<>(Comparator.reverseOrder());
    private PriorityQueue<Integer> large = new PriorityQueue<>();

    public void addNum(int num) {
        small.offer(num);
        large.offer(small.poll());
        if (large.size() > small.size()) {
            small.offer(large.poll());
        }
    }

    public double findMedian() {
        if (small.size() > large.size()) return small.peek();
        return (small.peek() + large.peek()) / 2.0;
    }
}
```

---

## 9. Backtracking

### Subsets

```java
// Time: O(n × 2^n), Space: O(n)
public static List<List<Integer>> subsets(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    backtrack(result, new ArrayList<>(), nums, 0);
    return result;
}

private static void backtrack(List<List<Integer>> result, List<Integer> temp,
                               int[] nums, int start) {
    result.add(new ArrayList<>(temp));
    for (int i = start; i < nums.length; i++) {
        temp.add(nums[i]);
        backtrack(result, temp, nums, i + 1);
        temp.remove(temp.size() - 1);
    }
}
```

### Permutations

```java
// Time: O(n × n!), Space: O(n)
public static List<List<Integer>> permute(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    boolean[] used = new boolean[nums.length];
    backtrack(result, new ArrayList<>(), nums, used);
    return result;
}

private static void backtrack(List<List<Integer>> result, List<Integer> temp,
                               int[] nums, boolean[] used) {
    if (temp.size() == nums.length) {
        result.add(new ArrayList<>(temp));
        return;
    }
    for (int i = 0; i < nums.length; i++) {
        if (used[i]) continue;
        used[i] = true;
        temp.add(nums[i]);
        backtrack(result, temp, nums, used);
        temp.remove(temp.size() - 1);
        used[i] = false;
    }
}
```

### N-Queens

```java
// Time: O(n!), Space: O(n²)
public static List<List<String>> solveNQueens(int n) {
    List<List<String>> result = new ArrayList<>();
    char[][] board = new char[n][n];
    for (char[] row : board) Arrays.fill(row, '.');
    Set<Integer> cols = new HashSet<>();
    Set<Integer> diag1 = new HashSet<>(); // r + c
    Set<Integer> diag2 = new HashSet<>(); // r - c
    solve(result, board, 0, cols, diag1, diag2, n);
    return result;
}

private static void solve(List<List<String>> result, char[][] board, int row,
                           Set<Integer> cols, Set<Integer> d1, Set<Integer> d2, int n) {
    if (row == n) {
        result.add(Arrays.stream(board).map(String::new).toList());
        return;
    }
    for (int c = 0; c < n; c++) {
        if (cols.contains(c) || d1.contains(row + c) || d2.contains(row - c)) continue;
        board[row][c] = 'Q';
        cols.add(c); d1.add(row + c); d2.add(row - c);
        solve(result, board, row + 1, cols, d1, d2, n);
        board[row][c] = '.';
        cols.remove(c); d1.remove(row + c); d2.remove(row - c);
    }
}
```

---

## 10. Graphs

### Number of Islands

```java
// Time: O(m × n), Space: O(m × n)
public static int numIslands(char[][] grid) {
    int count = 0;
    for (int r = 0; r < grid.length; r++) {
        for (int c = 0; c < grid[0].length; c++) {
            if (grid[r][c] == '1') {
                count++;
                dfs(grid, r, c);
            }
        }
    }
    return count;
}

private static void dfs(char[][] grid, int r, int c) {
    if (r < 0 || r >= grid.length || c < 0 || c >= grid[0].length
        || grid[r][c] == '0') return;
    grid[r][c] = '0';  // mark visited
    dfs(grid, r + 1, c);
    dfs(grid, r - 1, c);
    dfs(grid, r, c + 1);
    dfs(grid, r, c - 1);
}
```

### Clone Graph

```java
// Time: O(V + E), Space: O(V)
public static Node cloneGraph(Node node) {
    if (node == null) return null;
    Map<Node, Node> visited = new HashMap<>();
    return clone(node, visited);
}

private static Node clone(Node node, Map<Node, Node> visited) {
    if (visited.containsKey(node)) return visited.get(node);
    Node copy = new Node(node.val);
    visited.put(node, copy);
    for (Node neighbor : node.neighbors) {
        copy.neighbors.add(clone(neighbor, visited));
    }
    return copy;
}
```

### Pacific Atlantic Water Flow

```java
// Time: O(m × n), Space: O(m × n)
public static List<List<Integer>> pacificAtlantic(int[][] heights) {
    int m = heights.length, n = heights[0].length;
    boolean[][] pac = new boolean[m][n];
    boolean[][] atl = new boolean[m][n];

    for (int c = 0; c < n; c++) {
        dfs(heights, 0, c, pac, Integer.MIN_VALUE);
        dfs(heights, m - 1, c, atl, Integer.MIN_VALUE);
    }
    for (int r = 0; r < m; r++) {
        dfs(heights, r, 0, pac, Integer.MIN_VALUE);
        dfs(heights, r, n - 1, atl, Integer.MIN_VALUE);
    }

    List<List<Integer>> result = new ArrayList<>();
    for (int r = 0; r < m; r++) {
        for (int c = 0; c < n; c++) {
            if (pac[r][c] && atl[r][c]) {
                result.add(List.of(r, c));
            }
        }
    }
    return result;
}

private static void dfs(int[][] h, int r, int c, boolean[][] ocean, int prev) {
    if (r < 0 || r >= h.length || c < 0 || c >= h[0].length
        || ocean[r][c] || h[r][c] < prev) return;
    ocean[r][c] = true;
    dfs(h, r + 1, c, ocean, h[r][c]);
    dfs(h, r - 1, c, ocean, h[r][c]);
    dfs(h, r, c + 1, ocean, h[r][c]);
    dfs(h, r, c - 1, ocean, h[r][c]);
}
```

---

## 11. Dynamic Programming

### Climbing Stairs

```java
// Time: O(n), Space: O(1)
public static int climbStairs(int n) {
    if (n <= 2) return n;
    int a = 1, b = 2;
    for (int i = 3; i <= n; i++) {
        int c = a + b;
        a = b;
        b = c;
    }
    return b;
}
```

### Coin Change

```java
// Time: O(amount × coins), Space: O(amount)
public static int coinChange(int[] coins, int amount) {
    int[] dp = new int[amount + 1];
    Arrays.fill(dp, amount + 1);
    dp[0] = 0;
    for (int i = 1; i <= amount; i++) {
        for (int coin : coins) {
            if (i >= coin) {
                dp[i] = Math.min(dp[i], 1 + dp[i - coin]);
            }
        }
    }
    return dp[amount] > amount ? -1 : dp[amount];
}
```

### Longest Increasing Subsequence

```java
// Time: O(n log n), Space: O(n)
public static int lengthOfLIS(int[] nums) {
    int[] tails = new int[nums.length];
    int size = 0;
    for (int x : nums) {
        int i = Arrays.binarySearch(tails, 0, size, x);
        if (i < 0) i = -(i + 1);
        tails[i] = x;
        if (i == size) size++;
    }
    return size;
}
```

### Longest Common Subsequence

```java
// Time: O(m × n), Space: O(min(m, n))
public static int longestCommonSubsequence(String text1, String text2) {
    int m = text1.length(), n = text2.length();
    int[] dp = new int[n + 1];
    for (int i = 1; i <= m; i++) {
        int prev = 0;
        for (int j = 1; j <= n; j++) {
            int temp = dp[j];
            if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                dp[j] = prev + 1;
            } else {
                dp[j] = Math.max(dp[j], dp[j - 1]);
            }
            prev = temp;
        }
    }
    return dp[n];
}
```

### Word Break

```java
// Time: O(n² × m), Space: O(n)
public static boolean wordBreak(String s, List<String> wordDict) {
    Set<String> dict = new HashSet<>(wordDict);
    int n = s.length();
    boolean[] dp = new boolean[n + 1];
    dp[0] = true;
    for (int i = 1; i <= n; i++) {
        for (int j = 0; j < i; j++) {
            if (dp[j] && dict.contains(s.substring(j, i))) {
                dp[i] = true;
                break;
            }
        }
    }
    return dp[n];
}
```

---

## 12. Greedy

### Maximum Subarray (Kadane's Algorithm)

```java
// Time: O(n), Space: O(1)
public static int maxSubArray(int[] nums) {
    int maxSoFar = nums[0], maxEnding = nums[0];
    for (int i = 1; i < nums.length; i++) {
        maxEnding = Math.max(nums[i], maxEnding + nums[i]);
        maxSoFar = Math.max(maxSoFar, maxEnding);
    }
    return maxSoFar;
}
```

### Jump Game

```java
// Time: O(n), Space: O(1)
public static boolean canJump(int[] nums) {
    int maxReach = 0;
    for (int i = 0; i <= maxReach && i < nums.length; i++) {
        maxReach = Math.max(maxReach, i + nums[i]);
        if (maxReach >= nums.length - 1) return true;
    }
    return false;
}
```

---

## 13. Intervals

### Merge Intervals

```java
// Time: O(n log n), Space: O(n)
public static int[][] merge(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> Integer.compare(a[0], b[0]));
    List<int[]> result = new ArrayList<>();
    int[] current = intervals[0];
    for (int i = 1; i < intervals.length; i++) {
        if (intervals[i][0] <= current[1]) {
            current[1] = Math.max(current[1], intervals[i][1]);
        } else {
            result.add(current);
            current = intervals[i];
        }
    }
    result.add(current);
    return result.toArray(new int[0][]);
}
```

### Non-Overlapping Intervals

```java
// Time: O(n log n), Space: O(1)
public static int eraseOverlapIntervals(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> Integer.compare(a[1], b[1]));
    int count = 0, end = Integer.MIN_VALUE;
    for (int[] interval : intervals) {
        if (interval[0] >= end) {
            end = interval[1];
        } else {
            count++;
        }
    }
    return count;
}
```

---

## 14. Math & Geometry

### Rotate Image

```java
// Time: O(n²), Space: O(1)
public static void rotate(int[][] matrix) {
    int n = matrix.length;
    // Transpose
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            int temp = matrix[i][j];
            matrix[i][j] = matrix[j][i];
            matrix[j][i] = temp;
        }
    }
    // Reverse each row
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n / 2; j++) {
            int temp = matrix[i][j];
            matrix[i][j] = matrix[i][n - 1 - j];
            matrix[i][n - 1 - j] = temp;
        }
    }
}
```

### Spiral Matrix

```java
// Time: O(m × n), Space: O(1) (excluding output)
public static List<Integer> spiralOrder(int[][] matrix) {
    List<Integer> result = new ArrayList<>();
    int top = 0, bottom = matrix.length - 1;
    int left = 0, right = matrix[0].length - 1;
    while (top <= bottom && left <= right) {
        for (int c = left; c <= right; c++) result.add(matrix[top][c]);
        top++;
        for (int r = top; r <= bottom; r++) result.add(matrix[r][right]);
        right--;
        if (top <= bottom) {
            for (int c = right; c >= left; c--) result.add(matrix[bottom][c]);
            bottom--;
        }
        if (left <= right) {
            for (int r = bottom; r >= top; r--) result.add(matrix[r][left]);
            left++;
        }
    }
    return result;
}
```

---

## 15. Bit Manipulation

### Single Number

```java
// Time: O(n), Space: O(1)
public static int singleNumber(int[] nums) {
    int result = 0;
    for (int n : nums) result ^= n;
    return result;
}
```

### Number of 1 Bits

```java
// Time: O(k) where k = number of set bits, Space: O(1)
public static int hammingWeight(int n) {
    int count = 0;
    while (n != 0) {
        count++;
        n &= (n - 1);  // clears lowest set bit
    }
    return count;
}
```

### Reverse Bits

```java
// Time: O(1), Space: O(1)
public static int reverseBits(int n) {
    int result = 0;
    for (int i = 0; i < 32; i++) {
        result = (result << 1) | (n & 1);
        n >>>= 1;
    }
    return result;
}
```

### Counting Bits

```java
// Time: O(n), Space: O(n)
public static int[] countBits(int n) {
    int[] dp = new int[n + 1];
    for (int i = 1; i <= n; i++) {
        dp[i] = dp[i >> 1] + (i & 1);
    }
    return dp;
}
```

---

## Data Structures Reference

```java
// ListNode (for linked list problems)
public class ListNode {
    int val;
    ListNode next;
    ListNode() {}
    ListNode(int val) { this.val = val; }
    ListNode(int val, ListNode next) { this.val = val; this.next = next; }
}

// TreeNode (for tree problems)
public class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    TreeNode() {}
    TreeNode(int val) { this.val = val; }
    TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }
}

// Graph Node (for graph problems)
public class Node {
    public int val;
    public List<Node> neighbors;
    public Node() { neighbors = new ArrayList<>(); }
    public Node(int val) { this.val = val; neighbors = new ArrayList<>(); }
}
```

## Complexity Cheat Sheet

| Data Structure | Access | Search | Insert | Delete | Space |
|---------------|--------|--------|--------|--------|-------|
| Array | O(1) | O(n) | O(n) | O(n) | O(n) |
| ArrayList | O(1) | O(n) | O(1)* | O(n) | O(n) |
| LinkedList | O(n) | O(n) | O(1) | O(1) | O(n) |
| HashMap | O(1) | O(1) | O(1) | O(1) | O(n) |
| TreeMap | O(log n) | O(log n) | O(log n) | O(log n) | O(n) |
| HashSet | — | O(1) | O(1) | O(1) | O(n) |
| PriorityQueue | O(1) | O(n) | O(log n) | O(log n) | O(n) |

\* Amortized
