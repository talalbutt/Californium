/*******************************************************************************
 * Copyright (c) 2012, Institute for Pervasive Computing, ETH Zurich.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * 
 * This file is part of the Californium (Cf) CoAP framework.
 ******************************************************************************/

package ch.ethz.inf.vs.californium.endpoint.resources;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

import ch.ethz.inf.vs.californium.coap.DELETERequest;
import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.registries.CodeRegistry;
import ch.ethz.inf.vs.californium.coap.registries.MediaTypeRegistry;

/**
 * Resource that encapsulate the proxy functionalities.
 * 
 * 
 * @author Francesco Corazza
 * 
 */
public class ProxyStatsResource extends LocalResource {

	private final ConcurrentHashMap<String, Integer> resourceMap = new ConcurrentHashMap<String, Integer>();
	private final ConcurrentHashMap<String, Integer> addressMap = new ConcurrentHashMap<String, Integer>();

	/**
	 * Instantiates a new proxy resource.
	 */
	public ProxyStatsResource() {
		super("proxy/stats");
		setTitle("Keeps track of the requests served by the proxy.");
		// setResourceType("Proxy");
		isObservable(true);
	}

	@Override
	public void performDELETE(DELETERequest request) {
		resourceMap.clear();
		addressMap.clear();
		request.respond(CodeRegistry.RESP_DELETED);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * ch.ethz.inf.vs.californium.endpoint.LocalResource#performGET(ch.ethz.
	 * inf.vs.californium.coap.GETRequest)
	 */
	@Override
	public void performGET(GETRequest request) {
		String payload = getStatistics();
		request.respond(CodeRegistry.RESP_CONTENT, payload, MediaTypeRegistry.TEXT_PLAIN);
	}

	/**
	 * @param request
	 */
	public void updateStatistics(URI proxyUri) {
		if (proxyUri == null) {
			throw new IllegalArgumentException("proxyUri == null");
		}

		// manage the address requester
		String addressString = proxyUri.getHost();
		if (addressString != null) {

			// get the count of requests forwarded from the specific address
			Integer addressCount = addressMap.get(addressString);

			if (addressCount == null) {
				// initialize the values
				addressCount = new Integer(1);
				addressMap.put(addressString, addressCount);
			} else {
				// increment the counter
				addressCount++;
				// add the count to the map
				addressMap.put(addressString, addressCount);
			}
		}

		// manage the resource requested
		String resourceString = proxyUri.getPath();
		if (resourceString != null) {
			// get the count of requests forwarded to the resource
			Integer resourceCount = resourceMap.get(resourceString);

			if (resourceCount == null) {
				// initialize the values
				resourceCount = new Integer(1);
				resourceMap.put(resourceString, resourceCount);
			} else {
				// increment the counter
				resourceCount++;
				// add the count to the map
				resourceMap.replace(resourceString, resourceCount);
			}
		}
	}

	private String getStatistics() {
		StringBuilder builder = new StringBuilder();

		// get/print the clients served
		builder.append("Addresses served: " + addressMap.size() + "\n");
		for (String key : addressMap.keySet()) {
			builder.append("Host: " + key + " requests: " + addressMap.get(key) + " times\n");
		}

		builder.append("\n");

		// get/print the resources requested
		builder.append("Resources requested: " + resourceMap.size() + "\n");
		for (String key : resourceMap.keySet()) {
			builder.append("Resource " + key + " requested: " + resourceMap.get(key) + " times\n");
		}

		return builder.toString();
	}
}
