/*
   Copyright 2019 Nationale-Nederlanden

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package nl.nn.adapterframework.extensions.cmis;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import nl.nn.adapterframework.core.IPipeLineSession;
import nl.nn.adapterframework.util.LogUtil;
import nl.nn.adapterframework.util.XmlBuilder;
import nl.nn.adapterframework.util.XmlUtils;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.CreatablePropertyTypes;
import org.apache.chemistry.opencmis.commons.data.NewTypeSettableAttributes;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.PolicyIdList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.MutablePropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.MutableTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeMutability;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityOrderBy;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.SupportedPermissions;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CreatablePropertyTypesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.NewTypeSettableAttributesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PolicyIdListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.TypeMutabilityImpl;
import org.apache.chemistry.opencmis.server.support.TypeDefinitionFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CmisUtils {

	public final static String FORMATSTRING_BY_DEFAULT = "yyyy-MM-dd'T'HH:mm:ss";
	public static final String ORIGINAL_OBJECT_KEY = "originalObject";

	private static Logger log = LogUtil.getLogger(CmisUtils.class);

	public static XmlBuilder buildXml(String name, Object value) {
		XmlBuilder filterXml = new XmlBuilder(name);

		if(value != null)
			filterXml.setValue(value.toString());

		return filterXml;
	}

	public static XmlBuilder getPropertyXml(PropertyData<?> property) {
		XmlBuilder propertyXml = new XmlBuilder("property");
		String name = property.getId();
		propertyXml.addAttribute("name", name);
		Object value = property.getFirstValue();

		if (value == null) {
			propertyXml.addAttribute("isNull", "true");
		}
		if (value instanceof BigInteger) {
			BigInteger bi = (BigInteger) property.getFirstValue();
			propertyXml.setValue(String.valueOf(bi));
			propertyXml.addAttribute("type", "integer");
		} else if (value instanceof Boolean) {
			Boolean b = (Boolean) property.getFirstValue();
			propertyXml.setValue(String.valueOf(b));
			propertyXml.addAttribute("type", "boolean");
		} else if (value instanceof GregorianCalendar) {
			GregorianCalendar gc = (GregorianCalendar) property.getFirstValue();
			SimpleDateFormat sdf = new SimpleDateFormat(FORMATSTRING_BY_DEFAULT);
			propertyXml.setValue(sdf.format(gc.getTime()));
			propertyXml.addAttribute("type", "datetime");
		} else {
			propertyXml.setValue((String) property.getFirstValue());
		}
		return propertyXml;
	}

	public static Properties processProperties(Element cmisElement) {
		PropertiesImpl properties = new PropertiesImpl();

		Element propertiesElement = XmlUtils.getFirstChildTag(cmisElement, "properties");
		Iterator<Node> propertyIterator = XmlUtils.getChildTags(propertiesElement, "property").iterator();
		while (propertyIterator.hasNext()) {
			Element propertyElement = (Element) propertyIterator.next();
			String propertyValue = XmlUtils.getStringValue(propertyElement);
			String nameAttr = propertyElement.getAttribute("name");
			String typeAttr = propertyElement.getAttribute("type");
			PropertyType propertyType = PropertyType.STRING;
			if(StringUtils.isNotEmpty(typeAttr))
				propertyType = PropertyType.fromValue(typeAttr);

			boolean isNull = Boolean.parseBoolean(propertyElement.getAttribute("isNull"));
			if(isNull)
				propertyValue = null;

			if (StringUtils.isEmpty(typeAttr) || typeAttr.equalsIgnoreCase("string")) {
				PropertyStringDefinitionImpl propertyDefinition = new PropertyStringDefinitionImpl();
				propertyDefinition.setId(nameAttr);
				propertyDefinition.setDisplayName(nameAttr);
				propertyDefinition.setLocalName(nameAttr);
				propertyDefinition.setQueryName(nameAttr);
				propertyDefinition.setCardinality(Cardinality.SINGLE);
				propertyDefinition.setPropertyType(propertyType);

				if(nameAttr.startsWith("cmis:")) {
					propertyDefinition.setPropertyType(PropertyType.ID);
					properties.addProperty(new PropertyIdImpl(propertyDefinition, propertyValue));
				}
				else {
					propertyDefinition.setPropertyType(PropertyType.STRING);
					properties.addProperty(new PropertyStringImpl(propertyDefinition, propertyValue));
				}
			} else if (typeAttr.equalsIgnoreCase("integer")) {

				PropertyIntegerDefinitionImpl propertyDefinition = new PropertyIntegerDefinitionImpl();
				propertyDefinition.setId(nameAttr);
				propertyDefinition.setDisplayName(nameAttr);
				propertyDefinition.setLocalName(nameAttr);
				propertyDefinition.setQueryName(nameAttr);
				propertyDefinition.setCardinality(Cardinality.SINGLE);
				propertyDefinition.setPropertyType(propertyType);

				properties.addProperty(new PropertyIntegerImpl(propertyDefinition, new BigInteger(propertyValue)));
			} else if (typeAttr.equalsIgnoreCase("boolean")) {

				PropertyBooleanDefinitionImpl propertyDefinition = new PropertyBooleanDefinitionImpl();
				propertyDefinition.setId(nameAttr);
				propertyDefinition.setDisplayName(nameAttr);
				propertyDefinition.setLocalName(nameAttr);
				propertyDefinition.setQueryName(nameAttr);
				propertyDefinition.setCardinality(Cardinality.SINGLE);
				propertyDefinition.setPropertyType(propertyType);

				properties.addProperty(new PropertyBooleanImpl(propertyDefinition, Boolean.parseBoolean(propertyValue)));
			} else if (typeAttr.equalsIgnoreCase("datetime")) {

				PropertyDateTimeDefinitionImpl propertyDefinition = new PropertyDateTimeDefinitionImpl();
				propertyDefinition.setId(nameAttr);
				propertyDefinition.setDisplayName(nameAttr);
				propertyDefinition.setLocalName(nameAttr);
				propertyDefinition.setQueryName(nameAttr);
				propertyDefinition.setCardinality(Cardinality.SINGLE);
				propertyDefinition.setPropertyType(propertyType);

				String formatStringAttr = propertyElement.getAttribute("formatString");
				if (StringUtils.isEmpty(formatStringAttr)) {
					formatStringAttr = CmisUtils.FORMATSTRING_BY_DEFAULT;
				}
				DateFormat df = new SimpleDateFormat(formatStringAttr);
				try {
					Date date = df.parse(propertyValue);
					GregorianCalendar gregorian = new GregorianCalendar();
					gregorian.setTime(date);

					properties.addProperty(new PropertyDateTimeImpl(propertyDefinition, gregorian));
				} catch (ParseException e) {
					log.warn("exception parsing date [" + propertyValue + "] using formatString [" + formatStringAttr + "]", e);
				}
			} else {
				log.warn("unparsable type [" + typeAttr + "] for property ["+propertyValue+"]");
			}
			log.debug("set property name [" + nameAttr + "] value [" + propertyValue + "]");
		}

		return properties;
	}

	public static XmlBuilder propertyDefintions2Xml(Map<String, PropertyDefinition<?>> propertyDefinitions) {
		XmlBuilder propertyDefinitionsXml = new XmlBuilder("propertyDefinitions");
		for (Entry<String, PropertyDefinition<?>> entry : propertyDefinitions.entrySet()) {
			XmlBuilder propertyDefinitionXml = new XmlBuilder("propertyDefinition");
			PropertyDefinition<?> definition = entry.getValue();
			propertyDefinitionXml.addAttribute("id", definition.getId());
			propertyDefinitionXml.addAttribute("displayName", definition.getDisplayName());
			propertyDefinitionXml.addAttribute("description", definition.getDescription());
			propertyDefinitionXml.addAttribute("localName", definition.getLocalName());
			propertyDefinitionXml.addAttribute("localNamespace", definition.getLocalNamespace());
			propertyDefinitionXml.addAttribute("queryName", definition.getQueryName());
			propertyDefinitionXml.addAttribute("cardinality", definition.getCardinality().toString());
			propertyDefinitionXml.addAttribute("propertyType", definition.getPropertyType().value());
			propertyDefinitionXml.addAttribute("updatability", definition.getUpdatability().toString());
			if(definition.isInherited() != null)
				propertyDefinitionXml.addAttribute("inherited", definition.isInherited());
			if(definition.isOpenChoice() != null)
				propertyDefinitionXml.addAttribute("openChoice", definition.isOpenChoice());
			if(definition.isOrderable() != null)
				propertyDefinitionXml.addAttribute("orderable", definition.isOrderable());
			if(definition.isQueryable() != null)
				propertyDefinitionXml.addAttribute("queryable", definition.isQueryable());
			if(definition.isRequired() != null)
				propertyDefinitionXml.addAttribute("required", definition.isRequired());
			if(definition.getDefaultValue() != null && definition.getDefaultValue().size() > 0) {
				String defValue = definition.getDefaultValue().get(0).toString();
				propertyDefinitionXml.addAttribute("defaultValue", defValue);
			}
			if(definition.getChoices() != null && definition.getChoices().size() > 0) {
				propertyDefinitionXml.addAttribute("choices", "not implemented");
			}
			propertyDefinitionsXml.addSubElement(propertyDefinitionXml);
		}
		return propertyDefinitionsXml;
	}

	public static XmlBuilder typeDefinition2Xml(ObjectType objectType) {
		XmlBuilder typeXml = new XmlBuilder("typeDefinition");
		typeXml.addAttribute("id", objectType.getId());
		typeXml.addAttribute("description", objectType.getDescription());
		typeXml.addAttribute("displayName", objectType.getDisplayName());
		typeXml.addAttribute("localName", objectType.getLocalName());
		typeXml.addAttribute("localNamespace", objectType.getLocalNamespace());
		typeXml.addAttribute("baseTypeId", objectType.getBaseTypeId().value());
		typeXml.addAttribute("parentTypeId", objectType.getParentTypeId());
		typeXml.addAttribute("queryName", objectType.getQueryName());

		if(objectType.isControllableAcl() != null)
			typeXml.addAttribute("controllableACL", objectType.isControllableAcl());
		if(objectType.isControllablePolicy() != null)
			typeXml.addAttribute("controllablePolicy", objectType.isControllablePolicy());
		if(objectType.isCreatable() != null)
			typeXml.addAttribute("creatable", objectType.isCreatable());
		if(objectType.isFileable() != null)
			typeXml.addAttribute("fileable", objectType.isFileable());
		if(objectType.isControllableAcl() != null)
			typeXml.addAttribute("fulltextIndexed", objectType.isFulltextIndexed());
		if(objectType.isIncludedInSupertypeQuery() != null)
			typeXml.addAttribute("includedInSupertypeQuery", objectType.isIncludedInSupertypeQuery());
		if(objectType.isQueryable() != null)
			typeXml.addAttribute("queryable", objectType.isQueryable());

		typeXml.addSubElement(CmisUtils.typeMutability2xml(objectType.getTypeMutability()));

		Map<String, PropertyDefinition<?>> propertyDefinitions = objectType.getPropertyDefinitions();
		if(propertyDefinitions != null) {
			typeXml.addSubElement(CmisUtils.propertyDefintions2Xml(propertyDefinitions));
		}

		return typeXml;
	}

	private static XmlBuilder typeMutability2xml(TypeMutability typeMutability) {
		XmlBuilder xmlBuilder = new XmlBuilder("typeMutability");
		if(typeMutability.canCreate() != null)
			xmlBuilder.addAttribute("create", typeMutability.canCreate());
		if(typeMutability.canDelete() != null)
			xmlBuilder.addAttribute("delete", typeMutability.canDelete());
		if(typeMutability.canUpdate() != null)
			xmlBuilder.addAttribute("update", typeMutability.canUpdate());
		return xmlBuilder;
	}

	private static TypeMutability xml2typeMutability(Element typeXml) {
		TypeMutabilityImpl typeMutability = new TypeMutabilityImpl();
		if(typeXml.hasAttribute("create"))
			typeMutability.setCanCreate(CmisUtils.parseBooleanAttr(typeXml, "create"));
		if(typeXml.hasAttribute("update"))
			typeMutability.setCanUpdate(CmisUtils.parseBooleanAttr(typeXml, "update"));
		if(typeXml.hasAttribute("delete"))
			typeMutability.setCanDelete(CmisUtils.parseBooleanAttr(typeXml, "delete"));
		return typeMutability;
	}

	public static TypeDefinition xml2TypeDefinition(Element typeXml, CmisVersion cmisVersion) {
		String id = typeXml.getAttribute("id");
		String description = typeXml.getAttribute("description");
		String displayName = typeXml.getAttribute("displayName");
		String localName = typeXml.getAttribute("localName");
		String localNamespace = typeXml.getAttribute("localNamespace");
		String baseTypeId = typeXml.getAttribute("baseTypeId");
		String parentId = typeXml.getAttribute("parentTypeId");
		String queryName = typeXml.getAttribute("queryName");

		TypeDefinitionFactory factory = TypeDefinitionFactory.newInstance();
		MutableTypeDefinition definition = null;

		if(BaseTypeId.CMIS_DOCUMENT.equals(BaseTypeId.fromValue(baseTypeId))) {
			definition = factory.createBaseDocumentTypeDefinition(cmisVersion);
		}
		else if(BaseTypeId.CMIS_FOLDER.equals(BaseTypeId.fromValue(baseTypeId))) {
			definition = factory.createBaseFolderTypeDefinition(cmisVersion);
		}
		else if(BaseTypeId.CMIS_ITEM.equals(BaseTypeId.fromValue(baseTypeId))) {
			definition = factory.createBaseItemTypeDefinition(cmisVersion);
		}
		else if(BaseTypeId.CMIS_POLICY.equals(BaseTypeId.fromValue(baseTypeId))) {
			definition = factory.createBasePolicyTypeDefinition(cmisVersion);
		}
		else if(BaseTypeId.CMIS_RELATIONSHIP.equals(BaseTypeId.fromValue(baseTypeId))) {
			definition = factory.createBaseRelationshipTypeDefinition(cmisVersion);
		}
		else if(BaseTypeId.CMIS_SECONDARY.equals(BaseTypeId.fromValue(baseTypeId))) {
			definition = factory.createBaseSecondaryTypeDefinition(cmisVersion);
		}
		definition.setDescription(description);
		definition.setDisplayName(displayName);
		definition.setId(id);
		definition.setLocalName(localName);
		definition.setLocalNamespace(localNamespace);
		definition.setParentTypeId(parentId);
		definition.setQueryName(queryName);

		Element propertyDefinitions = XmlUtils.getFirstChildTag(typeXml, "propertyDefinitions");
		Collection<Node> propertyDefinitionList = XmlUtils.getChildTags(propertyDefinitions, "propertyDefinition");
		if(propertyDefinitionList != null) {
			for (Node node : propertyDefinitionList) {
				definition.addPropertyDefinition(CmisUtils.xml2PropertyDefinition((Element) node));
			}
		}

		definition.setIsControllableAcl(CmisUtils.parseBooleanAttr(typeXml, "controllableACL"));
		definition.setIsControllablePolicy(CmisUtils.parseBooleanAttr(typeXml, "controllablePolicy"));
		definition.setIsCreatable(CmisUtils.parseBooleanAttr(typeXml, "creatable"));
		definition.setIsFileable(CmisUtils.parseBooleanAttr(typeXml, "fileable"));
		definition.setIsFulltextIndexed(CmisUtils.parseBooleanAttr(typeXml, "fulltextIndexed"));
		definition.setIsIncludedInSupertypeQuery(CmisUtils.parseBooleanAttr(typeXml, "includedInSupertypeQuery"));
		definition.setIsQueryable(CmisUtils.parseBooleanAttr(typeXml, "queryable"));

		Element typeMutabilityXml = XmlUtils.getFirstChildTag(typeXml, "typeMutability");
		if(typeMutabilityXml != null) {
			definition.setTypeMutability(CmisUtils.xml2typeMutability(typeMutabilityXml));
		}

		return definition;
	}

	private static PropertyDefinition<?> xml2PropertyDefinition(Element propertyDefinitionXml) {
		MutablePropertyDefinition<?> definition = null;

		PropertyType type = PropertyType.fromValue(propertyDefinitionXml.getAttribute("propertyType"));
		switch (type) {
		case ID:
			definition = new PropertyIdDefinitionImpl();
			break;
		case BOOLEAN:
			definition = new PropertyBooleanDefinitionImpl();
			break;
		case DATETIME:
			definition = new PropertyDateTimeDefinitionImpl();
			break;
		case DECIMAL:
			definition = new PropertyDecimalDefinitionImpl();
			break;
		case INTEGER:
			definition = new PropertyIntegerDefinitionImpl();
			break;
		case HTML:
			definition = new PropertyHtmlDefinitionImpl();
			break;
		case URI:
			definition = new PropertyUriDefinitionImpl();
			break;
		case STRING:
		default:
			definition = new PropertyStringDefinitionImpl();
			break;
		}

		definition.setPropertyType(type);
		definition.setId(propertyDefinitionXml.getAttribute("id"));
		definition.setDisplayName(CmisUtils.parseStringAttr(propertyDefinitionXml, "displayName"));
		definition.setDescription(CmisUtils.parseStringAttr(propertyDefinitionXml, "description"));
		definition.setLocalName(CmisUtils.parseStringAttr(propertyDefinitionXml, "localName"));
		definition.setLocalNamespace(CmisUtils.parseStringAttr(propertyDefinitionXml, "localNamespace"));
		definition.setQueryName(CmisUtils.parseStringAttr(propertyDefinitionXml, "queryName"));

		if(propertyDefinitionXml.hasAttribute("cardinality")) {
			definition.setCardinality(Cardinality.valueOf(propertyDefinitionXml.getAttribute("cardinality")));
		}
		if(propertyDefinitionXml.hasAttribute("updatability")) {
			definition.setUpdatability(Updatability.valueOf(propertyDefinitionXml.getAttribute("updatability")));
		}

		definition.setIsInherited(CmisUtils.parseBooleanAttr(propertyDefinitionXml, "inherited"));
		definition.setIsOpenChoice(CmisUtils.parseBooleanAttr(propertyDefinitionXml, "openChoice"));
		definition.setIsOrderable(CmisUtils.parseBooleanAttr(propertyDefinitionXml, "orderable"));
		definition.setIsQueryable(CmisUtils.parseBooleanAttr(propertyDefinitionXml, "queryable"));
		definition.setIsRequired(CmisUtils.parseBooleanAttr(propertyDefinitionXml, "required"));

		if(propertyDefinitionXml.hasAttribute("defaultValue")) {
			//TODO: turn this into a list
			List defaultValues = new ArrayList();
			String defaultValue = propertyDefinitionXml.getAttribute("defaultValue");
			defaultValues.add(defaultValue);
			definition.setDefaultValue(defaultValues);
		}

		return definition;
	}
	/**
	 * Helper class
	 */
	private static String parseStringAttr(Element xml, String attribute) {
		if(xml.hasAttribute(attribute)) {
			return xml.getAttribute(attribute);
		}
		return null;
	}

	/**
	 * Helper class because Boolean can also be NULL in some cases with CMIS
	 */
	private static Boolean parseBooleanAttr(Element xml, String attribute) {
		if(xml.hasAttribute(attribute)) {
			return Boolean.parseBoolean(xml.getAttribute(attribute));
		}
		return null;
	}

	/**
	 * Helper class because BigInteger can also be NULL in some cases with CMIS
	 */
	private static BigInteger parseBigIntegerAttr(Element xml, String attribute) {
		if(xml.hasAttribute(attribute)) {
			String value = xml.getAttribute(attribute);
			Long longValue = Long.parseLong(value);
			return BigInteger.valueOf(longValue);
		}
		return null;
	}
	

	public static XmlBuilder repositoryInfo2xml(RepositoryInfo repository) {
		XmlBuilder repositoryXml = new XmlBuilder("repository");
		repositoryXml.addAttribute("cmisVersion", repository.getCmisVersion().value());
		repositoryXml.addAttribute("cmisVersionSupported", repository.getCmisVersionSupported());
		repositoryXml.addAttribute("description", repository.getDescription());
		repositoryXml.addAttribute("id", repository.getId());
		repositoryXml.addAttribute("latestChangeLogToken", repository.getLatestChangeLogToken());
		repositoryXml.addAttribute("name", repository.getName());
		repositoryXml.addAttribute("principalIdAnonymous", repository.getPrincipalIdAnonymous());
		repositoryXml.addAttribute("principalIdAnyone", repository.getPrincipalIdAnyone());
		repositoryXml.addAttribute("productName", repository.getProductName());
		repositoryXml.addAttribute("productVersion", repository.getProductVersion());
		repositoryXml.addAttribute("rootFolderId", repository.getRootFolderId());
		repositoryXml.addAttribute("thinClientUri", repository.getThinClientUri());
		repositoryXml.addAttribute("vendorName", repository.getVendorName());
		repositoryXml.addAttribute("changesIncomplete", repository.getChangesIncomplete());
		repositoryXml.addSubElement(CmisUtils.aclCapabilities2xml(repository.getAclCapabilities()));
		repositoryXml.addSubElement(CmisUtils.repositoryCapabilities2xml(repository.getCapabilities()));
		repositoryXml.addSubElement(CmisUtils.changesOnType2xml(repository.getChangesOnType()));

		return repositoryXml;
	}

	private static XmlBuilder aclCapabilities2xml(AclCapabilities aclCapabilities) {
		XmlBuilder aclCapabilitiesXml = new XmlBuilder("aclCapabilities");
		if(aclCapabilities != null) {
			aclCapabilitiesXml.addAttribute("aclPropagation", aclCapabilities.getAclPropagation().name());
			aclCapabilitiesXml.addAttribute("supportedPermissions", aclCapabilities.getSupportedPermissions().name());
			aclCapabilitiesXml.addSubElement(permissionMapping2xml(aclCapabilities.getPermissionMapping()));
			aclCapabilitiesXml.addSubElement(permissionDefinitionList2xml(aclCapabilities.getPermissions()));
		}
		return aclCapabilitiesXml;
	}

	private static XmlBuilder permissionMapping2xml(Map<String, PermissionMapping> permissionMapping) {
		XmlBuilder permissionMappingXml = new XmlBuilder("permissionMapping");
		for (Entry<String, PermissionMapping> entry : permissionMapping.entrySet()) {
			XmlBuilder permissionXml = new XmlBuilder("permission");
			permissionXml.addAttribute("name", entry.getKey());
			PermissionMapping mapping = entry.getValue();
			StringBuilder types = new StringBuilder();

			for (String permission : mapping.getPermissions()) {
				if(types.length() > 0)
					types.append(",");

				types.append(permission);
			}
			permissionXml.setValue(types.toString());
			permissionMappingXml.addSubElement(permissionXml);
		}
		return permissionMappingXml;
	}

	private static XmlBuilder permissionDefinitionList2xml(List<PermissionDefinition> list) {
		XmlBuilder permissionsXml = new XmlBuilder("permissions");

		for (PermissionDefinition permission : list) {
			XmlBuilder permissionXml = new XmlBuilder("permissions");
			permissionXml.addAttribute("id", permission.getId());
			permissionXml.addAttribute("description", permission.getDescription());
			permissionsXml.addSubElement(permissionXml);
		}
		return permissionsXml;
	}

	private static XmlBuilder repositoryCapabilities2xml(RepositoryCapabilities capabilities) {
		XmlBuilder repositoryXml = new XmlBuilder("repositoryCapabilities");
		if(capabilities != null) {
			if(capabilities.isAllVersionsSearchableSupported() != null)
				repositoryXml.addAttribute("allVersionsSearchable", capabilities.isAllVersionsSearchableSupported());
			if(capabilities.isGetDescendantsSupported() != null)
				repositoryXml.addAttribute("supportsGetDescendants", capabilities.isGetDescendantsSupported());
			if(capabilities.isGetFolderTreeSupported() != null)
				repositoryXml.addAttribute("supportsGetFolderTree", capabilities.isGetFolderTreeSupported());
			if(capabilities.isMultifilingSupported() != null)
				repositoryXml.addAttribute("supportsMultifiling", capabilities.isMultifilingSupported());
			if(capabilities.isPwcSearchableSupported() != null)
				repositoryXml.addAttribute("isPwcSearchable", capabilities.isPwcSearchableSupported());
			if(capabilities.isPwcUpdatableSupported() != null)
				repositoryXml.addAttribute("isPwcUpdatable", capabilities.isPwcUpdatableSupported());
			if(capabilities.isUnfilingSupported() != null)
				repositoryXml.addAttribute("supportsUnfiling", capabilities.isUnfilingSupported());
			if(capabilities.isVersionSpecificFilingSupported() != null)
				repositoryXml.addAttribute("supportsVersionSpecificFiling", capabilities.isVersionSpecificFilingSupported());

			repositoryXml.addAttribute("aclCapability", capabilities.getAclCapability().name());
			repositoryXml.addAttribute("changesCapability", capabilities.getChangesCapability().name());
			repositoryXml.addAttribute("contentStreamUpdatesCapability", capabilities.getContentStreamUpdatesCapability().name());
			repositoryXml.addSubElement(CmisUtils.creatablePropertyTypes2xml(capabilities.getCreatablePropertyTypes()));
			repositoryXml.addAttribute("joinCapability", capabilities.getJoinCapability().name());
			repositoryXml.addSubElement(CmisUtils.newTypeSettableAttributes2xml(capabilities.getNewTypeSettableAttributes()));
			repositoryXml.addAttribute("orderByCapability", capabilities.getOrderByCapability().name());
			repositoryXml.addAttribute("queryCapability", capabilities.getQueryCapability().name());
			repositoryXml.addAttribute("renditionsCapability", capabilities.getRenditionsCapability().name());
		}
		return repositoryXml;
	}

	private static XmlBuilder creatablePropertyTypes2xml(CreatablePropertyTypes creatablePropertyTypes) {
		XmlBuilder creatablePropertyTypesXml = new XmlBuilder("creatablePropertyTypes");
		if(creatablePropertyTypes != null) {
			for (PropertyType propertyType : creatablePropertyTypes.canCreate()) {
				creatablePropertyTypesXml.addSubElement(CmisUtils.buildXml("type", propertyType.name()));
			}
		}

		return creatablePropertyTypesXml;
	}

	private static XmlBuilder newTypeSettableAttributes2xml(NewTypeSettableAttributes newTypeSettableAttributes) {
		XmlBuilder newTypeSettableAttributesXml = new XmlBuilder("newTypeSettableAttributes");

		if(newTypeSettableAttributes.canSetControllableAcl() != null)
			newTypeSettableAttributesXml.addAttribute("canSetControllableAcl", newTypeSettableAttributes.canSetControllableAcl());
		if(newTypeSettableAttributes.canSetControllablePolicy() != null)
			newTypeSettableAttributesXml.addAttribute("canSetControllablePolicy", newTypeSettableAttributes.canSetControllablePolicy());
		if(newTypeSettableAttributes.canSetCreatable() != null)
			newTypeSettableAttributesXml.addAttribute("canSetCreatable", newTypeSettableAttributes.canSetCreatable());
		if(newTypeSettableAttributes.canSetDescription() != null)
			newTypeSettableAttributesXml.addAttribute("canSetDescription", newTypeSettableAttributes.canSetDescription());
		if(newTypeSettableAttributes.canSetDisplayName() != null)
			newTypeSettableAttributesXml.addAttribute("canSetDisplayName", newTypeSettableAttributes.canSetDisplayName());
		if(newTypeSettableAttributes.canSetFileable() != null)
			newTypeSettableAttributesXml.addAttribute("canSetFileable", newTypeSettableAttributes.canSetFileable());
		if(newTypeSettableAttributes.canSetFulltextIndexed() != null)
			newTypeSettableAttributesXml.addAttribute("canSetFulltextIndexed", newTypeSettableAttributes.canSetFulltextIndexed());
		if(newTypeSettableAttributes.canSetId() != null)
			newTypeSettableAttributesXml.addAttribute("canSetId", newTypeSettableAttributes.canSetId());
		if(newTypeSettableAttributes.canSetIncludedInSupertypeQuery() != null)
			newTypeSettableAttributesXml.addAttribute("canSetIncludedInSupertypeQuery", newTypeSettableAttributes.canSetIncludedInSupertypeQuery());
		if(newTypeSettableAttributes.canSetLocalName() != null)
			newTypeSettableAttributesXml.addAttribute("canSetLocalName", newTypeSettableAttributes.canSetLocalName());
		if(newTypeSettableAttributes.canSetLocalNamespace() != null)
			newTypeSettableAttributesXml.addAttribute("canSetLocalNamespace", newTypeSettableAttributes.canSetLocalNamespace());
		if(newTypeSettableAttributes.canSetQueryable() != null)
			newTypeSettableAttributesXml.addAttribute("canSetQueryable", newTypeSettableAttributes.canSetQueryable());
		if(newTypeSettableAttributes.canSetQueryName() != null)
			newTypeSettableAttributesXml.addAttribute("canSetQueryName", newTypeSettableAttributes.canSetQueryName());

		return newTypeSettableAttributesXml;
	}

	private static XmlBuilder changesOnType2xml(List<BaseTypeId> list) {
		XmlBuilder changesOnTypeXml = new XmlBuilder("changesOnTypes");
		for (BaseTypeId baseTypeId : list) {
			changesOnTypeXml.addSubElement(CmisUtils.buildXml("type", baseTypeId.name()));
		}

		return changesOnTypeXml;
	}

	public static RepositoryInfo xml2repositoryInfo(Element cmisResult) {
		RepositoryInfoImpl repositoryInfo = new RepositoryInfoImpl();

		repositoryInfo.setCmisVersion(CmisVersion.fromValue(cmisResult.getAttribute("cmisVersion")));
		repositoryInfo.setCmisVersionSupported(cmisResult.getAttribute("cmisVersionSupported"));
		repositoryInfo.setDescription(cmisResult.getAttribute("description"));
		repositoryInfo.setId(cmisResult.getAttribute("id"));
		repositoryInfo.setLatestChangeLogToken(cmisResult.getAttribute("latestChangeLogToken"));
		repositoryInfo.setName(cmisResult.getAttribute("name"));
		repositoryInfo.setPrincipalAnonymous(cmisResult.getAttribute("principalIdAnonymous"));
		repositoryInfo.setPrincipalAnyone(cmisResult.getAttribute("principalIdAnyone"));
		repositoryInfo.setProductName(cmisResult.getAttribute("productName"));
		repositoryInfo.setProductVersion(cmisResult.getAttribute("productVersion"));
		repositoryInfo.setRootFolder(cmisResult.getAttribute("rootFolderId"));
		repositoryInfo.setThinClientUri(cmisResult.getAttribute("thinClientUri"));
		repositoryInfo.setVendorName(cmisResult.getAttribute("vendorName"));
		repositoryInfo.setChangesIncomplete(CmisUtils.parseBooleanAttr(cmisResult, "changesIncomplete"));
		repositoryInfo.setAclCapabilities(CmisUtils.xml2aclCapabilities(cmisResult));
		repositoryInfo.setCapabilities(CmisUtils.xml2capabilities(cmisResult));
		repositoryInfo.setChangesOnType(CmisUtils.xml2changesOnType(cmisResult));

		return repositoryInfo;
	}

	private static AclCapabilities xml2aclCapabilities(Element cmisResult) {
		AclCapabilitiesDataImpl aclCapabilities = new AclCapabilitiesDataImpl();
		
		Element aclCapabilitiesXml = XmlUtils.getFirstChildTag(cmisResult, "aclCapabilities");

		aclCapabilities.setAclPropagation(AclPropagation.valueOf(aclCapabilitiesXml.getAttribute("aclPropagation")));
		aclCapabilities.setSupportedPermissions(SupportedPermissions.valueOf(aclCapabilitiesXml.getAttribute("supportedPermissions")));

		aclCapabilities.setPermissionMappingData(CmisUtils.xml2permissionMapping(aclCapabilitiesXml));
		aclCapabilities.setPermissionDefinitionData(CmisUtils.xml2permissionDefinitionList(aclCapabilitiesXml));

		return aclCapabilities;
	}

	private static Map<String, PermissionMapping> xml2permissionMapping(Element cmisResult) {
		Map<String, PermissionMapping> permissionMap = new HashMap<String, PermissionMapping>();

		Element permissionMapXml = XmlUtils.getFirstChildTag(cmisResult, "permissionMapping");
		for (Node node : XmlUtils.getChildTags(permissionMapXml, "permission")) {
			Element element = (Element) node;
			String key = element.getAttribute("name");
			String types = XmlUtils.getStringValue(element);

			PermissionMappingDataImpl permissionMapData = new PermissionMappingDataImpl();
			List<String> permissions = new ArrayList<String>();
			StringTokenizer tokenizer = new StringTokenizer(types, ",");
			while (tokenizer.hasMoreTokens()) {
				permissions.add(tokenizer.nextToken());
			}
			permissionMapData.setPermissions(permissions);
			permissionMapData.setKey(key);
			permissionMap.put(key, permissionMapData);
		}
		return permissionMap;
	}

	private static List<PermissionDefinition> xml2permissionDefinitionList(Element cmisResult) {
		List<PermissionDefinition> permissionsList = new ArrayList<PermissionDefinition>();

		Element permissionsXml = XmlUtils.getFirstChildTag(cmisResult, "permissions");
		for (Node node : XmlUtils.getChildTags(permissionsXml, "permission")) {
			Element element = (Element) node;

			PermissionDefinitionDataImpl permissionDefinition = new PermissionDefinitionDataImpl();
			permissionDefinition.setId(element.getAttribute("id"));
			permissionDefinition.setDescription(element.getAttribute("description"));
			permissionsList.add(permissionDefinition);
		}
		return permissionsList;
	}

	private static RepositoryCapabilities xml2capabilities(Element cmisResult) {
		Element repositoryCapabilitiesXml = XmlUtils.getFirstChildTag(cmisResult, "repositoryCapabilities");
		RepositoryCapabilitiesImpl repositoryCapabilities = new RepositoryCapabilitiesImpl();

		repositoryCapabilities.setAllVersionsSearchable(CmisUtils.parseBooleanAttr(repositoryCapabilitiesXml, "allVersionsSearchable"));
		repositoryCapabilities.setSupportsGetDescendants(CmisUtils.parseBooleanAttr(repositoryCapabilitiesXml, "supportsGetDescendants"));
		repositoryCapabilities.setSupportsGetFolderTree(CmisUtils.parseBooleanAttr(repositoryCapabilitiesXml, "supportsGetFolderTree"));
		repositoryCapabilities.setSupportsMultifiling(CmisUtils.parseBooleanAttr(repositoryCapabilitiesXml, "supportsMultifiling"));
		repositoryCapabilities.setIsPwcSearchable(CmisUtils.parseBooleanAttr(repositoryCapabilitiesXml, "isPwcSearchable"));
		repositoryCapabilities.setIsPwcUpdatable(CmisUtils.parseBooleanAttr(repositoryCapabilitiesXml, "isPwcUpdatable"));
		repositoryCapabilities.setSupportsUnfiling(CmisUtils.parseBooleanAttr(repositoryCapabilitiesXml, "supportsUnfiling"));
		repositoryCapabilities.setSupportsVersionSpecificFiling(CmisUtils.parseBooleanAttr(repositoryCapabilitiesXml, "supportsVersionSpecificFiling"));
		repositoryCapabilities.setCapabilityAcl(CapabilityAcl.valueOf(repositoryCapabilitiesXml.getAttribute("aclCapability")));
		repositoryCapabilities.setCapabilityChanges(CapabilityChanges.valueOf(repositoryCapabilitiesXml.getAttribute("changesCapability")));
		repositoryCapabilities.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.valueOf(repositoryCapabilitiesXml.getAttribute("contentStreamUpdatesCapability")));
		repositoryCapabilities.setCreatablePropertyTypes(CmisUtils.xml2creatablePropertyTypes(repositoryCapabilitiesXml));
		repositoryCapabilities.setCapabilityJoin(CapabilityJoin.valueOf(repositoryCapabilitiesXml.getAttribute("joinCapability")));
		repositoryCapabilities.setNewTypeSettableAttributes(CmisUtils.xml2newTypeSettableAttributes(repositoryCapabilitiesXml));
		repositoryCapabilities.setCapabilityOrderBy(CapabilityOrderBy.valueOf(repositoryCapabilitiesXml.getAttribute("orderByCapability")));
		repositoryCapabilities.setCapabilityQuery(CapabilityQuery.valueOf(repositoryCapabilitiesXml.getAttribute("queryCapability")));
		repositoryCapabilities.setCapabilityRendition(CapabilityRenditions.valueOf(repositoryCapabilitiesXml.getAttribute("renditionsCapability")));

		return repositoryCapabilities;
	}

	private static NewTypeSettableAttributes xml2newTypeSettableAttributes(Element cmisResult) {
		Element newTypeSettableAttributesXml = XmlUtils.getFirstChildTag(cmisResult, "newTypeSettableAttributes");
		NewTypeSettableAttributesImpl newTypeSettableAttributes = new NewTypeSettableAttributesImpl();

		newTypeSettableAttributes.setCanSetControllableAcl(CmisUtils.parseBooleanAttr(newTypeSettableAttributesXml, "canSetControllableAcl"));
		newTypeSettableAttributes.setCanSetControllablePolicy(CmisUtils.parseBooleanAttr(newTypeSettableAttributesXml, "canSetControllablePolicy"));
		newTypeSettableAttributes.setCanSetCreatable(CmisUtils.parseBooleanAttr(newTypeSettableAttributesXml, "canSetCreatable"));
		newTypeSettableAttributes.setCanSetDescription(CmisUtils.parseBooleanAttr(newTypeSettableAttributesXml, "canSetDescription"));
		newTypeSettableAttributes.setCanSetDisplayName(CmisUtils.parseBooleanAttr(newTypeSettableAttributesXml, "canSetDisplayName"));
		newTypeSettableAttributes.setCanSetFileable(CmisUtils.parseBooleanAttr(newTypeSettableAttributesXml, "canSetFileable"));
		newTypeSettableAttributes.setCanSetFulltextIndexed(CmisUtils.parseBooleanAttr(newTypeSettableAttributesXml, "canSetFulltextIndexed"));
		newTypeSettableAttributes.setCanSetId(CmisUtils.parseBooleanAttr(newTypeSettableAttributesXml, "canSetId"));
		newTypeSettableAttributes.setCanSetIncludedInSupertypeQuery(CmisUtils.parseBooleanAttr(newTypeSettableAttributesXml, "canSetIncludedInSupertypeQuery"));
		newTypeSettableAttributes.setCanSetLocalName(CmisUtils.parseBooleanAttr(newTypeSettableAttributesXml, "canSetLocalName"));
		newTypeSettableAttributes.setCanSetLocalNamespace(CmisUtils.parseBooleanAttr(newTypeSettableAttributesXml, "canSetLocalNamespace"));
		newTypeSettableAttributes.setCanSetQueryable(CmisUtils.parseBooleanAttr(newTypeSettableAttributesXml, "canSetQueryable"));
		newTypeSettableAttributes.setCanSetQueryName(CmisUtils.parseBooleanAttr(newTypeSettableAttributesXml, "canSetQueryName"));

		return newTypeSettableAttributes;
	}

	private static CreatablePropertyTypes xml2creatablePropertyTypes(Element cmisResult) {
		CreatablePropertyTypesImpl creatablePropertyTypes = new CreatablePropertyTypesImpl();
		Element creatablePropertyTypesXml = XmlUtils.getFirstChildTag(cmisResult, "creatablePropertyTypes");
		if(creatablePropertyTypesXml != null) {
			Set<PropertyType> propertyTypeSet = new TreeSet<PropertyType>();
			for (Node type : XmlUtils.getChildTags(cmisResult, "type")) {
				String value = XmlUtils.getStringValue((Element) type);
				if(StringUtils.isNotEmpty(value))
					propertyTypeSet.add(PropertyType.valueOf(value));
			}
			creatablePropertyTypes.setCanCreate(propertyTypeSet);
		}
		return creatablePropertyTypes;
	}

	private static List<BaseTypeId> xml2changesOnType(Element cmisResult) {
		List<BaseTypeId> baseTypeIds = new ArrayList<BaseTypeId>();

		Element changesOnType = XmlUtils.getFirstChildTag(cmisResult, "changesOnTypes");
		if(changesOnType != null) {
			for (Node type : XmlUtils.getChildTags(cmisResult, "type")) {
				String value = XmlUtils.getStringValue((Element) type);
				if(StringUtils.isNotEmpty(value))
					baseTypeIds.add(BaseTypeId.valueOf(value));
			}
		}
		return baseTypeIds;
	}

	public static XmlBuilder typeDescendants2Xml(List<Tree<ObjectType>> objectTypes) {
		return typeDescendants2Xml(objectTypes, new XmlBuilder("typeDescendants"));
	}

	public static XmlBuilder typeDescendants2Xml(List<Tree<ObjectType>> objectTypes, XmlBuilder parent) {
		for (Tree<ObjectType> object : objectTypes) {
			XmlBuilder typeDescendantXml = new XmlBuilder("typeDescendant");
			typeDescendantXml.addSubElement(CmisUtils.typeDefinition2Xml(object.getItem()));

			typeDescendantXml.addSubElement(typeDescendants2Xml(object.getChildren(), new XmlBuilder("children")));

			parent.addSubElement(typeDescendantXml);
		}
		return parent;
	}

	public static List<TypeDefinitionContainer> xml2TypeDescendants(Element typeDefinitionsXml, CmisVersion cmisVersion) {
		List<TypeDefinitionContainer> typeDefinitionList = new ArrayList<TypeDefinitionContainer>();
		Collection<Node> typeDescendantList = XmlUtils.getChildTags(typeDefinitionsXml, "typeDescendant");
		for (Node node : typeDescendantList) {
			Element typeDefinition = XmlUtils.getFirstChildTag((Element) node, "typeDefinition");
			TypeDefinition typeDef = CmisUtils.xml2TypeDefinition(typeDefinition, cmisVersion);
			TypeDefinitionContainerImpl typeDefinitionContainer = new TypeDefinitionContainerImpl(typeDef);

			Element children = XmlUtils.getFirstChildTag((Element) node, "children");
			typeDefinitionContainer.setChildren(xml2TypeDescendants(children, cmisVersion));

			typeDefinitionList.add(typeDefinitionContainer);
		}
		return typeDefinitionList;
	}

	public static void cmisObject2Xml(XmlBuilder cmisXml, CmisObject object) {
		if(object.getProperties() != null) {
			XmlBuilder propertiesXml = new XmlBuilder("properties");
			for (Iterator<Property<?>> it = object.getProperties().iterator(); it.hasNext();) {
				Property<?> property = (Property<?>) it.next();
				propertiesXml.addSubElement(CmisUtils.getPropertyXml(property));
			}
			cmisXml.addSubElement(propertiesXml);
		}

		if(object.getAllowableActions() != null) {
			XmlBuilder allowableActionsXml = new XmlBuilder("allowableActions");
			Set<Action> actions = object.getAllowableActions().getAllowableActions();
			for (Action action : actions) {
				XmlBuilder actionXml = new XmlBuilder("action");
				actionXml.setValue(action.value());
				allowableActionsXml.addSubElement(actionXml);
			}
			cmisXml.addSubElement(allowableActionsXml);
		}

		if(object.getAcl() != null) {
			XmlBuilder isExactAclXml = new XmlBuilder("isExactAcl");
			isExactAclXml.setValue(object.getAcl().isExact().toString());
			cmisXml.addSubElement(isExactAclXml);
		}

		List<ObjectId> policies = object.getPolicyIds();
		if(policies != null) {
			XmlBuilder policiesXml = new XmlBuilder("policyIds");
			for (ObjectId objectId : policies) {
				XmlBuilder policyXml = new XmlBuilder("policyId");
				policyXml.setValue(objectId.getId());
				policiesXml.addSubElement(policyXml);
			}
			cmisXml.addSubElement(policiesXml);
		}

		XmlBuilder relationshipsXml = new XmlBuilder("relationships");
		List<Relationship> relationships = object.getRelationships();
		if(relationships != null) {
			for (Relationship relation : relationships) {
				XmlBuilder relationXml = new XmlBuilder("relation");
				relationXml.setValue(relation.getId());
				relationshipsXml.addSubElement(relationXml);
			}
		}
		cmisXml.addSubElement(relationshipsXml);
	}

	public static XmlBuilder objectData2Xml(ObjectData object) {
		return CmisUtils.objectData2Xml(object, new XmlBuilder("objectData"));
	}
	public static XmlBuilder objectData2Xml(ObjectData object, XmlBuilder cmisXml) {

		if(object.getProperties() != null) {
			XmlBuilder propertiesXml = new XmlBuilder("properties");
			for (Iterator<PropertyData<?>> it = object.getProperties().getPropertyList().iterator(); it.hasNext();) {
				propertiesXml.addSubElement(CmisUtils.getPropertyXml(it.next()));
			}
			cmisXml.addSubElement(propertiesXml);
		}

		if(object.getAllowableActions() != null) {
			XmlBuilder allowableActionsXml = new XmlBuilder("allowableActions");
			Set<Action> actions = object.getAllowableActions().getAllowableActions();
			for (Action action : actions) {
				XmlBuilder actionXml = new XmlBuilder("action");
				actionXml.setValue(action.value());
				allowableActionsXml.addSubElement(actionXml);
			}
			cmisXml.addSubElement(allowableActionsXml);
		}

		if(object.getAcl() != null) {
			XmlBuilder isExactAclXml = new XmlBuilder("isExactAcl");
			isExactAclXml.setValue(object.getAcl().isExact().toString());
			cmisXml.addSubElement(isExactAclXml);
		}

		cmisXml.addAttribute("id", object.getId());
		cmisXml.addAttribute("baseTypeId", object.getBaseTypeId().name());

		PolicyIdList policies = object.getPolicyIds();
		if(policies != null) {
			XmlBuilder policiesXml = new XmlBuilder("policyIds");
			for (String objectId : policies.getPolicyIds()) {
				XmlBuilder policyXml = new XmlBuilder("policyId");
				policyXml.setValue(objectId);
				policiesXml.addSubElement(policyXml);
			}
			cmisXml.addSubElement(policiesXml);
		}

		XmlBuilder relationshipsXml = new XmlBuilder("relationships");
		List<ObjectData> relationships = object.getRelationships();
		if(relationships != null) {
			for (ObjectData relation : relationships) {
				relationshipsXml.addSubElement(objectData2Xml(relation, new XmlBuilder("relation")));
			}
		}
		cmisXml.addSubElement(relationshipsXml);

		return cmisXml;
	}

	public static ObjectData xml2ObjectData(Element cmisElement, IPipeLineSession context) {
		ObjectDataImpl impl = new ObjectDataImpl();

		// Handle allowable actions
		Element allowableActionsElem = XmlUtils.getFirstChildTag(cmisElement, "allowableActions");
		if(allowableActionsElem != null) {
			AllowableActionsImpl allowableActions = new AllowableActionsImpl();
			Set<Action> actions = EnumSet.noneOf(Action.class);

			Iterator<Node> actionIterator = XmlUtils.getChildTags(allowableActionsElem, "action").iterator();
			while (actionIterator.hasNext()) {
				String property = XmlUtils.getStringValue((Element) actionIterator.next());
				actions.add(Action.fromValue(property));
			}

			allowableActions.setAllowableActions(actions);
			impl.setAllowableActions(allowableActions);
		}

		// Handle isExactAcl
		impl.setIsExactAcl(XmlUtils.getChildTagAsBoolean(cmisElement, "isExactAcl"));

		// If the original object exists copy the permissions over. These cannot (and shouldn't) be changed)
		if(context != null) {
			CmisObject object = (CmisObject) context.get(CmisUtils.ORIGINAL_OBJECT_KEY);
			if(object != null) {
				impl.setAcl(object.getAcl());
			}
		}

		// Handle policyIds
		Element policyIdsElem = XmlUtils.getFirstChildTag(cmisElement, "policyIds");
		if(policyIdsElem != null) {
			PolicyIdListImpl policyIdList = new PolicyIdListImpl();
			List<String> policies = new ArrayList<String>();

			Iterator<Node> policyIterator = XmlUtils.getChildTags(allowableActionsElem, "policyId").iterator();
			while (policyIterator.hasNext()) {
				String policyId = XmlUtils.getStringValue((Element) policyIterator.next());
				policies.add(policyId);
			}

			policyIdList.setPolicyIds(policies);
			impl.setPolicyIds(policyIdList);
		}

		// Handle properties
		impl.setProperties(CmisUtils.processProperties(cmisElement));

		Element relationshipsElem = XmlUtils.getFirstChildTag(cmisElement, "relationships");
		if(relationshipsElem != null) {
			List<ObjectData> relationships = new ArrayList<ObjectData>();
			for (Node type : XmlUtils.getChildTags(relationshipsElem, "relation")) {
				ObjectData data = xml2ObjectData((Element) type, null);
				relationships.add(data);
			}
			impl.setRelationships(relationships);
		}

		impl.setRenditions(null);
		impl.setExtensions(null);
		impl.setChangeEventInfo(null);

		return impl;
	}

	public static ObjectList xml2ObjectList(Element result, IPipeLineSession context) {
		ObjectListImpl objectList = new ObjectListImpl();
		objectList.setNumItems(CmisUtils.parseBigIntegerAttr(result, "numberOfItems"));
		objectList.setHasMoreItems(CmisUtils.parseBooleanAttr(result, "hasMoreItems"));

		List<ObjectData> objects = new ArrayList<ObjectData>();

		Element objectsElem = XmlUtils.getFirstChildTag(result, "objects");
		for (Node type : XmlUtils.getChildTags(objectsElem, "objectData")) {
			ObjectData objectData = xml2ObjectData((Element) type, context);
			objects.add(objectData);
		}
		objectList.setObjects(objects);

		return objectList;
	}

	public static XmlBuilder objectList2xml(ObjectList result) {
		XmlBuilder objectListXml = new XmlBuilder("objectList");
		if(result.getNumItems() != null)
			objectListXml.addAttribute("numberOfItems", result.getNumItems().toString());
		if(result.hasMoreItems() != null)
			objectListXml.addAttribute("hasMoreItems", result.hasMoreItems());

		XmlBuilder objectDataXml = new XmlBuilder("objects");
		for (ObjectData objectData : result.getObjects()) {
			objectDataXml.addSubElement(CmisUtils.objectData2Xml(objectData));
		}
		objectListXml.addSubElement(objectDataXml);

		return objectListXml;
	}
}
