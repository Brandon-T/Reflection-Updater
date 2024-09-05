package acid.structures;

import acid.other.Utilities;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.function.Function;

/**
 * Created by Kira on 2014-12-06.
 */
public class ClassInfo {
    private String id;
    private String name;
    private ArrayList<ClassField> fields;
    private static LinkedHashMap<String, String> simbaMap;


    public ClassInfo(String id, String name) {
        this.id = id;
        this.name = name;
        this.fields = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setField(ClassField field) {
        int i = 0;
        for (; i < fields.size(); ++i) {
            if (fields.get(i).getId().equalsIgnoreCase(field.getId())) {
                fields.remove(i);
                break;
            }
        }

        fields.add(i, field);
    }

    public void putField(ClassField field) {
        int i = 0;
        for (i = 0; i < fields.size(); ++i) {
            if (fields.get(i).getId().equalsIgnoreCase(field.getId())) {
                fields.remove(i);
                break;
            }
        }

        for (i = 0; i < fields.size(); ++i) {
            if (fields.get(i).getId().contains("*")) {
                break;
            }
        }

        if (field.getId().contains("*")) {
            fields.add(field);
        } else {
            fields.add(i, field);
        }
    }

    public ClassField getField(String id) {
        for (ClassField field : fields) {
            if (field.getId().equalsIgnoreCase(id)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String result = Utilities.padLeft(" ", 4) + Utilities.padMiddle(this.id + ":", this.name + "\n", 25);
        result += Utilities.padLeft("\n", 34, '-');
        for (ClassField field : fields) {
            result += field + "\n";
        }

        return result + "\n";
    }

    public String toNativeHeaderString() {
        String result = "/**  " + this.getId() + "  **/\n";
        result += "extern Hook HOOK_" + this.getId().toUpperCase() + "_CLASS;\n";

        for (ClassField field : fields) {
            if (!field.getId().contains("*")) {
                result += "extern Hook HOOK_" + this.getId().toUpperCase() + "_" + field.getId().toUpperCase() + ";";
                result += "\n";
            }
        }

        return result + "\n";
    }

    public String toNativeString() {
        String result = "/**  " + this.getId() + "  **/\n";
        Function<String, String> QuoteString = (String stringToQuote) -> "\"" + stringToQuote + "\"";

        result += "Hook HOOK_" + this.getId().toUpperCase() + "_CLASS";
        result += " = {" + QuoteString.apply(this.getName()) + "};\n";

        for (ClassField field : fields) {
            if (!field.getId().contains("*")) {
                result += "Hook HOOK_" + this.getId().toUpperCase() + "_" + field.getId().toUpperCase();

                String owner = field.getOwner() != null ? field.getOwner() : this.getName();

                if (field.getMultiplier() != 0) {
                    if (field.getDesc() != null && field.getDesc().length() > 0) {
                        result += " = {" + QuoteString.apply(owner) + ", " + QuoteString.apply(field.getName()) + ", " + QuoteString.apply(field.getDesc()) + ", " + field.getMultiplier() + "};";
                    }
                    else {
                        result += " = {" + QuoteString.apply(owner) + ", " + QuoteString.apply(field.getName()) + ", " + field.getMultiplier() + "};";
                    }
                }
                else {
                    if (field.getDesc() != null && field.getDesc().length() > 0) {
                        result += " = {" + QuoteString.apply(owner) + ", " + QuoteString.apply(field.getName()) + ", " + QuoteString.apply(field.getDesc()) + "};";
                    }
                    else {
                        result += " = {" + QuoteString.apply(owner) + ", " + QuoteString.apply(field.getName()) + "};";
                    }
                }
                result += "\n";
            }
        }

        return result + "\n";
    }

    public String toSimbaString() {
        LinkedHashMap<String, String> simbaMap = this.buildSimbaMap();
        String result = "{" + simbaMap.getOrDefault(this.getId(), this.getId()) + ": " + this.getName() + "}\n";

        for (ClassField field : fields) {
            if (!field.getId().contains("*")) {
                long multi = field.getMultiplier();
                String beg = this.getId() + "_" + field.getId();
                beg = simbaMap.getOrDefault(beg, beg) + ": THook = ";

                String mid = "['" + (field.getOwner() != null ? field.getOwner() + "." + field.getName() : field.getName()) + "', " + (multi != 0 ? multi : 1) + "];";
                result += Utilities.padRight(beg, 50) + mid + "\n";
            }
        }

        return result;
    }

    public String toSimbaNativeString() {
        LinkedHashMap<String, String> simbaMap = this.buildSimbaMap();
        String result = "{" + simbaMap.getOrDefault(this.getId(), this.getId()) + ": " + this.getName() + "}\n";

        for (ClassField field : fields) {
            if (!field.getId().contains("*")) {
                long multi = field.getMultiplier();
                String beg = this.getId() + "_" + field.getId();
                beg = simbaMap.getOrDefault(beg, beg) + ": THook = ";

                String owner = field.getOwner() != null ? field.getOwner() : this.getName();
                String mid = "['" + owner + "', '" + field.getName() + "', '" + field.getDesc() + "', " + (multi != 0 ? multi : 1) + "];";
                result += Utilities.padRight(beg, 50) + mid + "\n";
            }
        }

        return result;
    }

    private String capitalize(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    private String camelCase(String line) {
        if (line.length() > 3) {
            return Character.toLowerCase(line.charAt(0)) + line.substring(1);
        }
        return line;
    }

    public void refactor(Collection<ClassNode> classes) {
        acid.other.Renamer r = new acid.other.Renamer(classes);

        fields.forEach(f -> {
            if (!f.getId().contains("*")) {
                if (f.getName().contains(".")) {
                    String c = f.getName().substring(0, f.getName().indexOf('.'));
                    String n = f.getName().substring(f.getName().indexOf('.') + 1);
                    r.renameField(c, n, f.getDesc(), camelCase(f.getId()), null);
                    r.renameField(capitalize(c), n, f.getDesc(), camelCase(f.getId()), null);
                } else {
                    r.renameField(this.getName(), f.getName(), f.getDesc(), camelCase(f.getId()), null);
                }
            } else {
                if (f.getName().length() < 3) {
                    r.renameMethod(this.getName(), f.getName(), f.getDesc(), camelCase(f.getId().replace("*", "")), null);
                }
            }
        });

        r.renameClass(this.getName(), this.getId());
    }

    private LinkedHashMap<String, String> buildSimbaMap() {
        if (simbaMap == null || simbaMap.size() == 0) {
            simbaMap = new LinkedHashMap<>();

            simbaMap.put("Node", "Node");
            simbaMap.put("Node_UID", "Node_UID");
            simbaMap.put("Node_Prev", "Node_Prev");
            simbaMap.put("Node_Next", "Node_Next");

            simbaMap.put("CacheableNode", "Cacheable");
            simbaMap.put("CacheableNode_Next", "Cacheable_Next");
            simbaMap.put("CacheableNode_Prev", "Cacheable_Prev");

            simbaMap.put("Animable", "Renderable");
            simbaMap.put("Animable_ModelHeight", "Renderable_ModelHeight");

            simbaMap.put("RenderableNode", "Animable");
            simbaMap.put("RenderableNode_ID", "Animable_ID");
            simbaMap.put("RenderableNode_Animation", "Animable_Animation");
            simbaMap.put("RenderableNode_Flags", "Animable_Flags");
            simbaMap.put("RenderableNode_Orientation", "Animable_Orientation");
            simbaMap.put("RenderableNode_Plane", "Animable_Plane");
            simbaMap.put("RenderableNode_X", "Animable_X");
            simbaMap.put("RenderableNode_Y", "Animable_Y");

            simbaMap.put("Model", "Model");
            simbaMap.put("Model_IndicesX", "Model_IndicesX");
            simbaMap.put("Model_IndicesY", "Model_IndicesY");
            simbaMap.put("Model_IndicesZ", "Model_IndicesZ");
            simbaMap.put("Model_IndicesLength", "Model_IndicesLength");
            simbaMap.put("Model_VerticesX", "Model_VerticesX");
            simbaMap.put("Model_VerticesY", "Model_VerticesY");
            simbaMap.put("Model_VerticesZ", "Model_VerticesZ");
            simbaMap.put("Model_VerticesLength", "Model_VerticesLength");
            simbaMap.put("Model_TexIndicesX", "Model_TexIndicesX");
            simbaMap.put("Model_TexIndicesY", "Model_TexIndicesY");
            simbaMap.put("Model_TexIndicesZ", "Model_TexIndicesZ");
            simbaMap.put("Model_TexVerticesX", "Model_TexVerticesX");
            simbaMap.put("Model_TexVerticesY", "Model_TexVerticesY");
            simbaMap.put("Model_TexVerticesZ", "Model_TexVerticesZ");
            simbaMap.put("Model_TexVerticesLength", "Model_TexVerticesLength");
            simbaMap.put("Model_ShadowIntensity", "Model_ShadowIntensity");
            simbaMap.put("Model_FitsSingleTile", "Model_FitsSingleTile");

            simbaMap.put("AnimationSequence", "AnimationSequence");
            simbaMap.put("AnimationSequence_ControlFlow", "AnimationSequence_ControlFlow");

            simbaMap.put("NPCDefinition", "NPCDefinition");
            simbaMap.put("NPCDefinition_ID", "NPCDefinition_ID");
            simbaMap.put("NPCDefinition_Name", "NPCDefinition_Name");
            simbaMap.put("NPCDefinition_Actions", "NPCDefinition_Actions");
            simbaMap.put("NPCDefinition_ModelIDs", "NPCDefinition_ModelIDs");
            simbaMap.put("NPCDefinition_CombatLevel", "NPCDefinition_CombatLevel");

            simbaMap.put("LinkedList", "LinkedList");
            simbaMap.put("LinkedList_Head", "LinkedList_Head");
            simbaMap.put("LinkedList_Current", "LinkedList_Current");

            simbaMap.put("Entity", "Actor");
            simbaMap.put("Entity_AnimationID", "Actor_Animation");
            simbaMap.put("Entity_AnimationDelay", "Actor_AnimationDelay");
            simbaMap.put("Entity_AnimationFrame", "Actor_AnimationFrame");
            simbaMap.put("Entity_MovementSequence", "Actor_MovementSequence");
            simbaMap.put("Entity_MovementFrame", "Actor_MovementFrame");
            simbaMap.put("Entity_CurrentSequence", "Actor_CurrentSequence");
            simbaMap.put("Entity_SpokenText", "Actor_SpokenText");
            simbaMap.put("Entity_HitDamages", "Actor_HitDamages");
            simbaMap.put("Entity_HitTypes", "Actor_HitTypes");
            simbaMap.put("Entity_HitCycle", "Actor_HitCycle");
            simbaMap.put("Entity_QueueX", "Actor_QueueX");
            simbaMap.put("Entity_QueueY", "Actor_QueueY");
            simbaMap.put("Entity_QueueTraversed", "Actor_QueueTraversed");
            simbaMap.put("Entity_QueueLength", "Actor_QueueSize");
            simbaMap.put("Entity_LocalX", "Actor_LocalX");
            simbaMap.put("Entity_LocalY", "Actor_LocalY");
            simbaMap.put("Entity_IsAnimating", "Actor_IsAnimating");
            simbaMap.put("Entity_CombatCycle", "Actor_CombatCycle");
            simbaMap.put("Entity_InteractingIndex", "Actor_InteractingIndex");
            simbaMap.put("Entity_Orientation", "Actor_Orientation");
            simbaMap.put("Entity_IsWalking", "Actor_IsWalking");
            simbaMap.put("Entity_TargetIndex", "Actor_TargetIndex");
            simbaMap.put("Entity_CombatInfoList", "Actor_CombatInfoList");
            simbaMap.put("Entity_Height", "Actor_Height");
            simbaMap.put("Entity_SpotAnimation", "Actor_SpotAnimation");
            simbaMap.put("Entity_SpotAnimationFrame", "Actor_SpotAnimationFrame");
            simbaMap.put("Entity_SpotAnimationFrameCycle", "Actor_SpotAnimationFrameCycle");
            simbaMap.put("Entity_GraphicsId", "Actor_GraphicsId");

            simbaMap.put("NPC", "NPC");
            simbaMap.put("NPC_Definition", "NPC_Definition");

            simbaMap.put("ObjectDefinition", "ObjectDefinition");

            simbaMap.put("Stream", "Stream");
            simbaMap.put("Stream_Payload", "Stream_Payload");
            simbaMap.put("Stream_CRC", "Stream_CRC");

            simbaMap.put("Widget", "Widget");
            simbaMap.put("Widget_Children", "Widget_Children");
            simbaMap.put("Widget_ID", "Widget_WidgetID");
            simbaMap.put("Widget_AbsoluteX", "Widget_AbsoluteX");
            simbaMap.put("Widget_AbsoluteY", "Widget_AbsoluteY");
            simbaMap.put("Widget_Width", "Widget_Width");
            simbaMap.put("Widget_Height", "Widget_Height");
            simbaMap.put("Widget_ParentID", "Widget_ParentID");
            simbaMap.put("Widget_IsHidden", "Widget_IsHidden");
            simbaMap.put("Widget_RelativeX", "Widget_RelativeX");
            simbaMap.put("Widget_RelativeY", "Widget_RelativeY");
            simbaMap.put("Widget_TextureID", "Widget_TextureID");
            simbaMap.put("Widget_Text", "Widget_Text");
            simbaMap.put("Widget_Name", "Widget_Name");
            simbaMap.put("Widget_ItemID", "Widget_ItemID");
            simbaMap.put("Widget_ItemAmount", "Widget_ItemAmount");
            simbaMap.put("Widget_BoundsIndex", "Widget_BoundsIndex");
            simbaMap.put("Widget_ScrollX", "Widget_ScrollX");
            simbaMap.put("Widget_ScrollY", "Widget_ScrollY");
            simbaMap.put("Widget_Items", "Widget_ItemIDs");
            simbaMap.put("Widget_ItemStackSizes", "Widget_StackSizes");
            simbaMap.put("Widget_Actions", "Widget_Actions");
            simbaMap.put("Widget_ActionType", "Widget_ActionType");
            simbaMap.put("Widget_Type", "Widget_Type");

            simbaMap.put("WidgetNode", "WidgetNode");
            simbaMap.put("WidgetNode_ID", "WidgetNode_ID");

            simbaMap.put("HashTable", "HashTable");
            simbaMap.put("HashTable_Cache|Buckets", "HashTable_Buckets");
            simbaMap.put("HashTable_Capacity", "HashTable_Size");
            simbaMap.put("HashTable_Index", "HashTable_Index");
            simbaMap.put("HashTable_Head", "HashTable_Head");
            simbaMap.put("HashTable_Tail", "HashTable_Tail");

            simbaMap.put("IterableHashTable", "IterableHashTable");
            simbaMap.put("IterableHashTable_Cache|Buckets", "IterableHashTable_Buckets");
            simbaMap.put("IterableHashTable_Capacity", "IterableHashTable_Size");
            simbaMap.put("IterableHashTable_Index", "IterableHashTable_Index");
            simbaMap.put("IterableHashTable_Head", "IterableHashTable_Head");
            simbaMap.put("IterableHashTable_Tail", "IterableHashTable_Tail");

            simbaMap.put("GameShell", "GameShell");

            simbaMap.put("Player", "Player");
            simbaMap.put("Player_Name", "Player_Name");
            simbaMap.put("Player_Definition", "Player_Definition");
            simbaMap.put("Player_CombatLevel", "Player_CombatLevel");
            simbaMap.put("Player_Model", "Player_Model");
            simbaMap.put("Player_Visible", "Player_Visible");

            simbaMap.put("GameInstance", "GameInstance");

            simbaMap.put("Client", "Client");
            simbaMap.put("Client_GameCycle", "Client_LoopCycle");
            simbaMap.put("Client_MenuOptions", "Client_MenuOptions");
            simbaMap.put("Client_MenuActions", "Client_MenuActions");
            simbaMap.put("Client_IsMenuOpen", "Client_IsMenuOpen");
            simbaMap.put("Client_MenuX", "Client_MenuX");
            simbaMap.put("Client_MenuY", "Client_MenuY");
            simbaMap.put("Client_MenuWidth", "Client_MenuWidth");
            simbaMap.put("Client_MenuHeight", "Client_MenuHeight");
            simbaMap.put("Client_MenuCount", "Client_MenuCount");
            simbaMap.put("Client_LocalPlayers", "Client_LocalPlayers");
            simbaMap.put("Client_Region", "Client_Region");
            simbaMap.put("Client_Plane", "Client_Plane");
            simbaMap.put("Client_DestY", "Client_DestinationY");
            simbaMap.put("Client_DestX", "Client_DestinationX");
            simbaMap.put("Client_LocalPlayer", "Client_LocalPlayer");
            simbaMap.put("Client_Widgets", "Client_Widgets");
            simbaMap.put("Client_WidgetSettings", "Client_GameSettings");
            simbaMap.put("Client_BaseX", "Client_BaseX");
            simbaMap.put("Client_BaseY", "Client_BaseY");
            simbaMap.put("Client_CurrentLevels", "Client_CurrentLevels");
            simbaMap.put("Client_RealLevels", "Client_RealLevels");
            simbaMap.put("Client_Experiences", "Client_Experiences");
            simbaMap.put("Client_Weight", "Client_Weight");
            simbaMap.put("Client_Energy", "Client_Energy");
            simbaMap.put("Client_CurrentWorld", "Client_CurrentWorld");
            simbaMap.put("Client_WidgetNodeCache", "Client_WidgetNodeCache");
            simbaMap.put("Client_TileSettings", "Client_TileSettings");
            simbaMap.put("Client_TileHeights", "Client_TileHeights");
            simbaMap.put("Client_LocalNpcs", "Client_LocalNpcs");
            simbaMap.put("Client_NpcIndices", "Client_NpcIndices");
            simbaMap.put("Client_CrosshairColour", "Client_CrossHairColor");
            simbaMap.put("Client_MapOffset", "Client_MapOffset");
            simbaMap.put("Client_MapAngle", "Client_MapAngle");
            simbaMap.put("Client_MapScale", "Client_MapScale");
            simbaMap.put("Client_CameraYaw", "Client_CameraYaw");
            simbaMap.put("Client_CosineTable", "Client_Cosine");
            simbaMap.put("Client_CameraPitch", "Client_CameraPitch");
            simbaMap.put("Client_SineTable", "Client_Sine");
            simbaMap.put("Client_CameraZ", "Client_CameraZ");
            simbaMap.put("Client_CameraY", "Client_CameraY");
            simbaMap.put("Client_CameraX", "Client_CameraX");
            simbaMap.put("Client_ViewportWidth", "Client_ViewportWidth");
            simbaMap.put("Client_CameraScale", "Client_CameraScale");
            simbaMap.put("Client_GroundItems", "Client_GroundItems");
            simbaMap.put("Client_LoginState", "Client_LoginState");
            simbaMap.put("Client_PlayerIndex", "Client_PlayerIndex");
            simbaMap.put("Client_WidgetPositionsX", "Client_WidgetPositionX");
            simbaMap.put("Client_WidgetPositionsY", "Client_WidgetPositionY");
            simbaMap.put("Client_WidgetWidths", "Client_WidgetWidths");
            simbaMap.put("Client_WidgetHeights", "Client_WidgetHeights");
            simbaMap.put("Client_EnergyLevel", "Client_Energy");
            simbaMap.put("Client_PlayerWeight", "Client_Weight");

            simbaMap.put("Region", "Region");
            simbaMap.put("Region_Tiles", "Region_SceneTiles");
            simbaMap.put("Region_GameObjects", "Region_GameObjects");

            simbaMap.put("Boundary", "BoundaryObject");
            simbaMap.put("Boundary_ID", "BoundaryObject_ID");
            simbaMap.put("Boundary_Flags", "BoundaryObject_Flags");
            simbaMap.put("Boundary_Plane", "BoundaryObject_Plane");
            simbaMap.put("Boundary_Height", "BoundaryObject_Height");
            simbaMap.put("Boundary_X", "BoundaryObject_LocalX");
            simbaMap.put("Boundary_Y", "BoundaryObject_LocalY");
            simbaMap.put("Boundary_Orientation", "BoundaryObject_Orientation");
            simbaMap.put("Boundary_Renderable", "BoundaryObject_Renderable");
            simbaMap.put("Boundary_OldRenderable", "BoundaryObject_Renderable2");
            simbaMap.put("Boundary_Height", "BoundaryObject_Height");

            simbaMap.put("Interactable", "GameObject");
            simbaMap.put("Interactable_ID", "GameObject_ID");
            simbaMap.put("Interactable_Flags", "GameObject_Flags");
            simbaMap.put("Interactable_Plane", "GameObject_Plane");
            simbaMap.put("Interactable_X", "GameObject_LocalX");
            simbaMap.put("Interactable_Y", "GameObject_LocalY");
            simbaMap.put("Interactable_RelativeX", "GameObject_WorldX");
            simbaMap.put("Interactable_RelativeY", "GameObject_WorldY");
            simbaMap.put("Interactable_SizeX", "GameObject_OffsetX");
            simbaMap.put("Interactable_SizeY", "GameObject_OffsetY");
            simbaMap.put("Interactable_Height", "GameObject_Height");
            simbaMap.put("Interactable_Renderable", "GameObject_Renderable");
            simbaMap.put("Interactable_Orientation", "GameObject_Orientation");

            simbaMap.put("GroundDecoration", "FloorDecoration");
            simbaMap.put("GroundDecoration_ID", "FloorDecoration_ID");
            simbaMap.put("GroundDecoration_Flags", "FloorDecoration_Flags");
            simbaMap.put("GroundDecoration_X", "FloorDecoration_LocalX");
            simbaMap.put("GroundDecoration_Y", "FloorDecoration_LocalY");
            simbaMap.put("GroundDecoration_Plane", "FloorDecoration_Plane");
            simbaMap.put("GroundDecoration_Renderable", "FloorDecoration_Renderable");

            simbaMap.put("WallDecoration", "WallDecoration");
            simbaMap.put("WallDecoration_ID", "WallDecoration_ID");
            simbaMap.put("WallDecoration_Flags", "WallDecoration_Flags");
            simbaMap.put("WallDecoration_Plane", "WallDecoration_Plane");
            simbaMap.put("WallDecoration_Height", "WallDecoration_Height");
            simbaMap.put("WallDecoration_X", "WallDecoration_LocalX");
            simbaMap.put("WallDecoration_Y", "WallDecoration_LocalY");
            simbaMap.put("WallDecoration_RelativeX", "WallDecoration_RelativeX");
            simbaMap.put("WallDecoration_RelativeY", "WallDecoration_RelativeY");
            simbaMap.put("WallDecoration_Orientation", "WallDecoration_Orientation");
            simbaMap.put("WallDecoration_Renderable", "WallDecoration_Renderable");
            simbaMap.put("WallDecoration_OldRenderable", "WallDecoration_Renderable2");

            simbaMap.put("SceneTile", "SceneTile");
            simbaMap.put("SceneTile_Boundary", "SceneTile_BoundaryObject");
            simbaMap.put("SceneTile_SceneTile", "SceneTile_SceneTileObject");
            simbaMap.put("SceneTile_Interactables", "SceneTile_GameObjects");
            simbaMap.put("SceneTile_WallDecoration", "SceneTile_WallDecoration");
            simbaMap.put("SceneTile_GroundDecoration", "SceneTile_GroundDecoration");
            simbaMap.put("SceneTile_SceneX", "SceneTile_SceneX");
            simbaMap.put("SceneTile_SceneY", "SceneTile_SceneY");
            simbaMap.put("SceneTile_Plane", "SceneTile_Plane");

            simbaMap.put("Item", "Item");
            simbaMap.put("Item_ID", "Item_ID");
            simbaMap.put("Item_Quantity", "Item_StackSizes");


            //Others
            simbaMap.put("TradingPost", "GrandExchange");
        }
        return simbaMap;
    }
}
