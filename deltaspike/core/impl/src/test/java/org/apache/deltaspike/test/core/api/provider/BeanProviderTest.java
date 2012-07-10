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
package org.apache.deltaspike.test.core.api.provider;


import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RunWith(Arquillian.class)
public class BeanProviderTest
{
    /**
     *X TODO creating a WebArchive is only a workaround because JavaArchive cannot contain other archives.
     */
    @Deployment
    public static WebArchive deploy()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "beanProviderTest.jar")
                .addPackage("org.apache.deltaspike.test.category")
                .addPackage("org.apache.deltaspike.test.core.api.provider")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class, "beanProvider.war")
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreArchive())
                .addAsLibraries(testJar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /**
     * lookup by type
     */
    @Test
    public void simpleBeanLookupByType()
    {
        TestBean testBean = BeanProvider.getContextualReference(TestBean.class, false);

        Assert.assertNotNull(testBean);
    }

    /**
     * lookup by name with expected type
     */
    @Test
    public void simpleBeanLookupByName()
    {
        TestBean testBean = BeanProvider.getContextualReference("extraNameBean", false, TestBean.class);

        Assert.assertNotNull(testBean);
    }

    /**
     * lookup by name without type
     */
    @Test
    public void simpleBeanLookupByNameWithoutType()
    {
        {
            // test with the convenient operator (optional=false implied)
            Object testBean = BeanProvider.getContextualReference("extraNameBean");
            Assert.assertNotNull(testBean);
            Assert.assertTrue(testBean instanceof TestBean);
            TestBean tb = (TestBean) testBean;
            Assert.assertEquals(4711, tb.getI());
        }

        {
            // test with the 'optional' flag set to false
            Object testBean = BeanProvider.getContextualReference("extraNameBean", false);
            Assert.assertNotNull(testBean);
            Assert.assertTrue(testBean instanceof TestBean);
            TestBean tb = (TestBean) testBean;
            Assert.assertEquals(4711, tb.getI());
        }

        {
            // test by name lookup with the 'optional' flag set to false
            try
            {
                Object testBean = BeanProvider.getContextualReference("thisBeanDoesNotExist");
                Assert.fail("BeanProvider#getContextualReference should have blown up with a non-existing bean!");
            }
            catch(IllegalStateException ise)
            {
                // all is well, this is exactly what should happen!
            }
        }

        {
            // test by type lookup with the 'optional' flag set to false
            try
            {
                // guess we can safely assume that there is no producer for a ConcurrentHashMap in the system...
                ConcurrentHashMap chm = BeanProvider.getContextualReference(ConcurrentHashMap.class);
                Assert.fail("BeanProvider#getContextualReference should have blown up with a non-existing bean!");
            }
            catch(IllegalStateException ise)
            {
                // all is well, this is exactly what should happen!
            }
        }


    }

    /*
     * lookup without result
     */
    @Test
    public void optionalBeanLookup()
    {
        NoBean result = BeanProvider.getContextualReference(NoBean.class, true);

        Assert.assertNull(result);
    }

    /*
     * lookup of all beans of a given type
     */
    @Test
    public void multiBeanLookupWithDependentBean() throws Exception
    {
        List<MultiBean> result = BeanProvider.getContextualReferences(MultiBean.class, false);

        Assert.assertNotNull(result);

        Assert.assertEquals(2, result.size());
    }

    /*
     * lookup of all beans of a given type which aren't dependent scoped
     */
    @Test
    public void multiBeanLookupWithoutDependentBean() throws Exception
    {
        List<MultiBean> result = BeanProvider.getContextualReferences(MultiBean.class, false, false);

        Assert.assertNotNull(result);

        Assert.assertEquals(1, result.size());
    }

    /*
     * create a manual instance, inject dependencies, set values of the dependencies and check the referenced cdi bean
     */
    @Test
    public void injectBeansInNonManagedInstance() throws Exception
    {
        ManualBean manualBean = new ManualBean();

        Assert.assertNull(manualBean.getTestBean());

        BeanProvider.injectFields(manualBean);

        Assert.assertNotNull(manualBean.getTestBean());

        Assert.assertEquals(4711, manualBean.getTestBean().getI2());

        int newValue = 14;

        manualBean.getTestBean().setI2(newValue);

        Assert.assertEquals(newValue, manualBean.getTestBean().getI2());

        TestBean testBean = BeanProvider.getContextualReference(TestBean.class);

        Assert.assertEquals(newValue, testBean.getI2());
    }
}
