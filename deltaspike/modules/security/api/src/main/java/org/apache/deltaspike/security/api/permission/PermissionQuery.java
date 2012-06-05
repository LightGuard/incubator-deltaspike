/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.security.api.permission;

import java.util.List;
import java.util.Set;

import org.apache.deltaspike.security.api.idm.IdentityType;
import org.apache.deltaspike.security.api.idm.Range;

/**
 * API for querying object permissions
 *
 */
public class PermissionQuery
{
    private Object resource;    
    private Set<Object> resources;
    private Range range;
    private IdentityType recipient;
    
    public Object getResource()
    {
        return resource;
    }
    
    public void setResource(Object resource)
    {
        this.resource = resource;
        this.resources = null;
    }
    
    public Set<Object> getResources()
    {
        return resources;
    }
    
    public void setResources(Set<Object> resources)
    {
        this.resources = resources;
        this.resource = null;
    }
    
    public Range getRange()
    {
        return range;
    }
    
    public void setRange(Range range)
    {
        this.range = range;
    }
    
    public IdentityType getRecipient()
    {
        return recipient;
    }
    
    public void setRecipient(IdentityType recipient)
    {
        this.recipient = recipient;
    }
    
    public List<Permission> getResultList() 
    {
        // TODO implement
        return null;
    }    
}
