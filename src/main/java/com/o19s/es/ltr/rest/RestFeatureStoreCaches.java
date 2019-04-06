/*
 * Copyright [2017] Wikimedia Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.o19s.es.ltr.rest;

import com.o19s.es.ltr.action.ClearCachesAction;
import com.o19s.es.ltr.action.ClearCachesAction.ClearCachesNodesResponse;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.action.RestActions.NodesResponseRestListener;
import org.elasticsearch.rest.action.RestBuilderListener;

import java.io.IOException;

import static org.elasticsearch.rest.RestStatus.OK;

/**
 * Clear cache (default store):
 * POST /_ltr/_clearcache
 *
 * Clear cache (custom store):
 * POST /_ltr/{store}/_clearcache
 *
 * Get cache stats (all stores)
 * GET /_ltr/_cachestats
 */
public class RestFeatureStoreCaches extends FeatureStoreBaseRestHandler {

    public RestFeatureStoreCaches(Settings settings, RestController controller) {
        super(settings);
        controller.registerHandler(RestRequest.Method.POST, "/_ltr/_clearcache", this);
        controller.registerHandler(RestRequest.Method.POST, "/_ltr/{store}/_clearcache", this);
        controller.registerHandler(RestRequest.Method.GET, "/_ltr/_cachestats", this);
    }

    @Override
    public String getName() {
        return "Provides clear cached for stores";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        if (request.method() == RestRequest.Method.POST) {
            return clearCache(request, client);
        } else {
            return getStats(client);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private RestChannelConsumer getStats(NodeClient client) {
        return (channel) -> new ClearCachesAction.RequestBuilder(client).execute(new NodesResponseRestListener(channel));
    }

    private RestChannelConsumer clearCache(RestRequest request, NodeClient client) {
        String storeName = indexName(request);
        ClearCachesAction.RequestBuilder builder = new ClearCachesAction.RequestBuilder(client);
        builder.request().clearStore(storeName);
        return (channel) -> builder.execute(new RestBuilderListener<ClearCachesNodesResponse>(channel) {
            @Override
            public RestResponse buildResponse(ClearCachesNodesResponse clearCachesNodesResponse,
                                              XContentBuilder builder) throws Exception {
                builder.startObject()
                        .field("acknowledged", true);
                builder.endObject();
                return new BytesRestResponse(OK, builder);
            }
        });
    }
}
