  /**
    @class
    <p>The GeoClusterFacet allows you to cluster results based on a GeoPoint.</p>

    <p>Facets are similar to SQL <code>GROUP BY</code> statements but perform much
       better. You can also construct several <em>"groups"</em> at once by simply
       specifying multiple facets.</p>
       
    <p>GeoClusterFacet is an interface for the Geo Cluster Facet Plugin: https://github.com/zenobase/geocluster-facet</p>

    <div class="alert-message block-message info">
        <p>
            <strong>Tip: </strong>
            For more information on faceted navigation, see
            <a href="http://en.wikipedia.org/wiki/Faceted_classification">this</a>
            Wikipedia article on Faceted Classification.
        </p>
    </div>

    @name ejs.GeoClusterFacet

    @desc
    <p>A facet that returns clusters for the given result set.</p>

    @param {String} name The name which be used to refer to this facet. For instance,
        the facet itself might utilize a field named <code>doc_authors</code>. Setting
        <code>name</code> to <code>Authors</code> would allow you to refer to the
        facet by that name, possibly simplifying some of the display logic.

    */
  ejs.GeoClusterFacet = function (name) {

    /**
        The internal facet object.
        @member ejs.GeoClusterFacet
        @property {Object} facet
        */
    var facet = {};
    facet[name] = {};
    facet[name].geo_cluster = {};

    return {

      /**
            <p>The name of a field of type `geo_point`.</p>

            @member ejs.GeoClusterFacet
            @param {Object} oFilter A valid <code>Query</code> object.
            @returns {Object} returns <code>this</code> so that calls can be chained.
            */
    	field: function (fieldName) {
	        
	        if (fieldName == null) {
	          return field;
	        }

	        facet[name].geo_cluster.field=fieldName;
	        
	        return this;
	      },

      
      
      /**
            <p>Controls the amount of clustering, from 0.0 (don't cluster any points) to 1.0 
            (create a single cluster containing all points). 
            Defaults to 0.1. This value is relative to the size of the area that contains points, 
            so it does not need to be adjusted e.g. when zooming in on a map.</p>

            @member ejs.GeoClusterFacet
            @param {String} path The nested path
            @returns {Object} returns <code>this</code> so that calls can be chained.
            */
      factor: function (factorValue) {
        if (factorValue == null) {
          return facet[name].factor;
        }
        
        facet[name].geo_cluster.factor = factorValue;
        return this;
      },
      
      /**
            <p>Allows you to serialize this object into a JSON encoded string.</p>

            @member ejs.GeoClusterFacet
            @returns {String} returns this object as a serialized JSON string.
            */
      toString: function () {
        return JSON.stringify(facet);
      },

      /**
            The type of ejs object.  For internal use only.
            
            @member ejs.GeoClusterFacet
            @returns {String} the type of object
            */
      _type: function () {
        return 'facet';
      },
      
      /**
            <p>Retrieves the internal <code>facet</code> object. This is typically used by
               internal API functions so use with caution.</p>

            @member ejs.GeoClusterFacet
            @returns {String} returns this object's internal <code>facet</code> property.
            */
      _self: function () {
        return facet;
      }
    };
  };
