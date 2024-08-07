package org.game3d.dev.engine.scene;

import lombok.Getter;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Node {
    private final List<Node> children;
    private final String name;
    private final Node parent;
    private final Matrix4f nodeTransformation;

    public Node(String name, Node parent, Matrix4f nodeTransformation) {
        this.name = name;
        this.parent = parent;
        this.nodeTransformation = nodeTransformation;
        this.children = new ArrayList<>();
    }

    public void addChild(Node child) {
        this.children.add(child);
    }
}
