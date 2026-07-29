package com.daqem.yamlconfig.api.config.entry.minecraft;

import com.daqem.yamlconfig.api.config.entry.IConfigEntry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;

public interface IIdentifierConfigEntry extends IConfigEntry<Identifier> {

    static StreamCodec<IIdentifierConfigEntry, NodeTuple> createCodec() {
        return StreamCodec.of(
                (identifierConfigEntry, node) -> {
                    if (node.getValueNode() instanceof ScalarNode scalarNode && scalarNode.getTag().equals(Tag.STR)) {
                        identifierConfigEntry.set(Identifier.parse(scalarNode.getValue()));
                    }
                },
                identifierConfigEntry -> {
                    ScalarNode keyNode = identifierConfigEntry.createKeyNode();
                    ScalarNode valueNode = new ScalarNode(Tag.STR, identifierConfigEntry.get().toString(), ScalarStyle.SINGLE_QUOTED);
                    return new NodeTuple(keyNode, valueNode);
                }
        );
    }

    String getPattern();
}
