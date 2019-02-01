# Metaverse Annotations

The AnnotationBasedStepMetaAnalyzer class can be used in place of
custom implementations to add metaverse graph information based
on a set of runtime annotations placed on the StepMetaInterface definition.

## @Node
Applies to a FIELD in the Meta class, designating a graph node in
the metaverse.
### Attributes
* **name** - the identifier of the node, used to connect it to @NodeLink and 
@Property elements.   
* **type** - the defined type of the node.  E.g. DictionaryConst.NODE_TYPE_DATASOURCE.  
* **link** - the link between a node and the parent step node.  By default set to
LINK_DEPENDENCYOF.

## @NodeLink
Applies to a FIELD. Defines a link between 2 @Node elements.
### Attributes
* **nodeName** - the name of the node associated with this field
* **parentNodeName**  - the node being linked to
* **parentNodeLink**  - the link type (defaults to LINK_CONTAINS)
* **linkDirection** -  Either IN or OUT

## @Property
Applies to a FIELD.  Defines a property associated with a @Node.
###Attributes
* **name** - the property name
* **type** - the property type, defaults to NODE_TYPE_TRANS_FIELD.
* **category** - the property's category, defaults to CATEGORY_FIELD
* **parentNodeName** - the node this property is associated with.

## @CategoryMap
Applies to the CLASS.  Defines a mapping between an entity and a category.
For example, could be used to map a new entity type (like `MQTT Server`) and
a category, (like CATEGORY_DATASOURCE)
###Attributes
* **entity** - the entity name (as defined in a @Node)
* **category** the Metaverse category to which this entity belongs

## @EntityLink
Applied to the CLASS.  Registers entity linking between different entity types.
I.e., calls DictionaryHelper.registerEntityType() with the annotated types.
* **entity** - the child entity
* **parentEntity** - the entity being linked to
* **link** - the link type.  E.g. LINK_CONTAINS_CONCEPT.


See the TestStepMeta in AnnotationDrivenStepMetaAnalyzerTest for an
example of these annotations applied to a meta.
