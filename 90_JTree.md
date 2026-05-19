# Lesson 90: JTree

## Key Concepts
- `JTree` for displaying hierarchical/tree data
- `DefaultMutableTreeNode` for creating tree nodes
- Building a tree structure by adding child nodes to parent nodes
- Wrapping the tree in a `JScrollPane`

## Code Example

```java
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JTree Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

        DefaultMutableTreeNode fruits = new DefaultMutableTreeNode("Fruits");
        fruits.add(new DefaultMutableTreeNode("Apple"));
        fruits.add(new DefaultMutableTreeNode("Banana"));
        fruits.add(new DefaultMutableTreeNode("Orange"));

        DefaultMutableTreeNode vegetables = new DefaultMutableTreeNode("Vegetables");
        vegetables.add(new DefaultMutableTreeNode("Carrot"));
        vegetables.add(new DefaultMutableTreeNode("Broccoli"));
        vegetables.add(new DefaultMutableTreeNode("Spinach"));

        DefaultMutableTreeNode dairy = new DefaultMutableTreeNode("Dairy");
        dairy.add(new DefaultMutableTreeNode("Milk"));
        dairy.add(new DefaultMutableTreeNode("Cheese"));
        dairy.add(new DefaultMutableTreeNode("Yogurt"));

        root.add(fruits);
        root.add(vegetables);
        root.add(dairy);

        JTree tree = new JTree(root);
        JScrollPane scrollPane = new JScrollPane(tree);
        frame.add(scrollPane);

        frame.setVisible(true);
    }
}
```

## Explanation
1. A **root** `DefaultMutableTreeNode` is created with the label "Root".
2. Three category nodes — "Fruits", "Vegetables", "Dairy" — are created and added to the root.
3. Individual items (e.g., "Apple", "Banana") are created as leaf nodes and added to their respective category nodes.
4. The entire tree is passed to the `JTree` constructor, which builds the expandable/collapsible tree UI.
5. The tree is wrapped in a `JScrollPane` to handle scrolling when the tree grows large.
6. Nodes with children (like "Fruits") can be expanded and collapsed by the user.

## Expected Output
- A window titled "JTree Example" displaying a tree structure:
  - **Root** (expandable)
    - **Fruits** (expandable)
      - Apple
      - Banana
      - Orange
    - **Vegetables** (expandable)
      - Carrot
      - Broccoli
      - Spinach
    - **Dairy** (expandable)
      - Milk
      - Cheese
      - Yogurt
- The user can click the triangle icons to expand or collapse each category.
