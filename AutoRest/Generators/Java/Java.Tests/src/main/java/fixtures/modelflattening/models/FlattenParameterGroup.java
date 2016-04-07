/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 * Changes may cause incorrect behavior and will be lost if the code is
 * regenerated.
 */

package fixtures.modelflattening.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * Additional parameters for the putSimpleProductWithGrouping operation.
 */
@JsonFlatten
public class FlattenParameterGroup {
    /**
     * Product name with value 'groupproduct'.
     */
    @JsonProperty(value = "", required = true)
    private String name;

    /**
     * Unique identifier representing a specific product for a given latitude
     * &amp; longitude. For example, uberX in San Francisco will have a
     * different product_id than uberX in Los Angeles.
     */
    @JsonProperty(value = "", required = true)
    private String productId;

    /**
     * Description of product.
     */
    @JsonProperty(value = "")
    private String description;

    /**
     * Display name of product.
     */
    @JsonProperty(value = "", required = true)
    private String maxProductDisplayName;

    /**
     * Generic URL value.
     */
    @JsonProperty(value = "")
    private String genericValue;

    /**
     * URL value.
     */
    @JsonProperty(value = "")
    private String odatavalue;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the FlattenParameterGroup object itself.
     */
    public FlattenParameterGroup setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the productId value.
     *
     * @return the productId value
     */
    public String productId() {
        return this.productId;
    }

    /**
     * Set the productId value.
     *
     * @param productId the productId value to set
     * @return the FlattenParameterGroup object itself.
     */
    public FlattenParameterGroup setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description value.
     *
     * @param description the description value to set
     * @return the FlattenParameterGroup object itself.
     */
    public FlattenParameterGroup setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the maxProductDisplayName value.
     *
     * @return the maxProductDisplayName value
     */
    public String maxProductDisplayName() {
        return this.maxProductDisplayName;
    }

    /**
     * Set the maxProductDisplayName value.
     *
     * @param maxProductDisplayName the maxProductDisplayName value to set
     * @return the FlattenParameterGroup object itself.
     */
    public FlattenParameterGroup setMaxProductDisplayName(String maxProductDisplayName) {
        this.maxProductDisplayName = maxProductDisplayName;
        return this;
    }

    /**
     * Get the genericValue value.
     *
     * @return the genericValue value
     */
    public String genericValue() {
        return this.genericValue;
    }

    /**
     * Set the genericValue value.
     *
     * @param genericValue the genericValue value to set
     * @return the FlattenParameterGroup object itself.
     */
    public FlattenParameterGroup setGenericValue(String genericValue) {
        this.genericValue = genericValue;
        return this;
    }

    /**
     * Get the odatavalue value.
     *
     * @return the odatavalue value
     */
    public String odatavalue() {
        return this.odatavalue;
    }

    /**
     * Set the odatavalue value.
     *
     * @param odatavalue the odatavalue value to set
     * @return the FlattenParameterGroup object itself.
     */
    public FlattenParameterGroup setOdatavalue(String odatavalue) {
        this.odatavalue = odatavalue;
        return this;
    }

}
