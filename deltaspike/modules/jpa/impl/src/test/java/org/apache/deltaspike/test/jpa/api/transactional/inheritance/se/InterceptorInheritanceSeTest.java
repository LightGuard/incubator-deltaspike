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

package org.apache.deltaspike.test.jpa.api.transactional.inheritance.se;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.util.ProjectStageProducer;
import org.apache.deltaspike.jpa.impl.transaction.context.TransactionContextExtension;
import org.apache.deltaspike.test.category.SeCategory;
import org.apache.deltaspike.test.jpa.api.shared.Child;
import org.apache.deltaspike.test.jpa.api.shared.TestEntityTransaction;
import org.apache.deltaspike.test.jpa.api.transactional.defaultnested.TestEntityManagerProducer;
import org.apache.deltaspike.test.jpa.api.transactional.inheritance.SubClass;
import org.apache.deltaspike.test.jpa.api.transactional.inheritance.SubClassAnnotated;
import org.apache.deltaspike.test.jpa.api.transactional.inheritance.SuperClass;
import org.apache.deltaspike.test.jpa.api.transactional.inheritance.SuperClassAnnotated;
import org.apache.deltaspike.test.util.ArchiveUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
@RunWith(Arquillian.class)
@Category(SeCategory.class)
public class InterceptorInheritanceSeTest
{
    @Inject
    private TestEntityManagerProducer entityManagerProducer;

    @Inject @Child
    private SubClass subClass;

    @Inject @Child
    private SubClassAnnotated subClassAnnotated;

    @Deployment
    public static Archive<?> createTestArchive()
    {
        JavaArchive testJar = ShrinkWrap.create(JavaArchive.class, "defaultNestedTransactionTest.jar")
                .addPackage(ArchiveUtils.SHARED_PACKAGE)
                .addClasses(SubClass.class, SuperClassAnnotated.class, TestEntityManagerProducer.class,
                        SubClassAnnotated.class, SuperClass.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        return ShrinkWrap.create(WebArchive.class)
                .addAsLibraries(ArchiveUtils.getDeltaSpikeCoreAndJpaArchive())
                .addAsLibraries(testJar)
                .addAsServiceProvider(Extension.class, TransactionContextExtension.class)
                .addAsWebInfResource(ArchiveUtils.getBeansXml(), "beans.xml");
    }

    @Before
    public void init()
    {
        ProjectStageProducer.setProjectStage(ProjectStage.UnitTest);
    }

    @Test
    public void assertInterceptorCascades()
    {
        TestEntityTransaction firstTransaction = (TestEntityTransaction) (entityManagerProducer.getFirstEntityManager())
                .getTransaction();

        subClass.subMethod();

        assertThat(firstTransaction.isActive(), is(false));
        assertThat(firstTransaction.isStarted(), is(true));
        assertThat(firstTransaction.isRolledBack(), is(false));
        assertThat(firstTransaction.getRollbackOnly(), is(false));
        assertThat(firstTransaction.isCommitted(), is(true));
    }

    @Test
    public void assertCallToSuperIntercepted()
    {
        TestEntityTransaction firstTransaction = (TestEntityTransaction) (entityManagerProducer.getFirstEntityManager())
                .getTransaction();

        subClass.superMethod();

        assertThat(firstTransaction.isActive(), is(false));
        assertThat(firstTransaction.isStarted(), is(true));
        assertThat(firstTransaction.isRolledBack(), is(false));
        assertThat(firstTransaction.getRollbackOnly(), is(false));
        assertThat(firstTransaction.isCommitted(), is(true));
    }

    @Test
    public void assertInterceptedChildInterceptsSuper()
    {
        TestEntityTransaction firstTransaction = (TestEntityTransaction) (entityManagerProducer.getFirstEntityManager())
                .getTransaction();

        subClassAnnotated.superMethod();

        assertThat(firstTransaction.isActive(), is(false));
        assertThat(firstTransaction.isStarted(), is(true));
        assertThat(firstTransaction.isRolledBack(), is(false));
        assertThat(firstTransaction.getRollbackOnly(), is(false));
        assertThat(firstTransaction.isCommitted(), is(true));
    }
}
