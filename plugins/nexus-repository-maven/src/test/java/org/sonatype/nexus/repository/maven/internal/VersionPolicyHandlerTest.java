/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.maven.internal;

import java.util.Arrays;
import java.util.Collection;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.maven.MavenFacet;
import org.sonatype.nexus.repository.maven.MavenPath;
import org.sonatype.nexus.repository.maven.MavenPathParser;
import org.sonatype.nexus.repository.maven.VersionPolicy;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD;
import static org.sonatype.nexus.repository.http.HttpMethods.PUT;
import static org.sonatype.nexus.repository.http.HttpStatus.BAD_REQUEST;
import static org.sonatype.nexus.repository.maven.VersionPolicy.MIXED;
import static org.sonatype.nexus.repository.maven.VersionPolicy.RELEASE;
import static org.sonatype.nexus.repository.maven.VersionPolicy.SNAPSHOT;

/**
 * Tests {@link VersionPolicyHandler}
 */
@RunWith(Parameterized.class)
public class VersionPolicyHandlerTest
    extends TestSupport
{
  @Mock
  private Context context;

  @Mock
  private Repository repository;

  @Mock
  private MavenFacet mavenFacet;

  @Mock
  private Response proceeded;

  @Mock
  private Request request;

  private VersionPolicyValidator versionPolicyValidator = new VersionPolicyValidator();

  private MavenPathParser mavenPathParser = new Maven2MavenPathParser();

  private VersionPolicyHandler underTest;

  @Before
  public void setup() {
    underTest = new VersionPolicyHandler(versionPolicyValidator);
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {SNAPSHOT, PUT, "org/sonatype/foo/1.0.0/foo-1.0.0.jar", false},
        {RELEASE, PUT, "org/sonatype/foo/1.0.0/foo-1.0.0.jar", true},
        {MIXED, PUT, "org/sonatype/foo/1.0.0/foo-1.0.0.jar", true},
        {SNAPSHOT, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar", true},
        {RELEASE, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar", false},
        {MIXED, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar", true},
        {SNAPSHOT, PUT, "org/sonatype/foo/maven-metadata.xml", true},
        {RELEASE, PUT, "org/sonatype/foo/maven-metadata.xml", true},
        {MIXED, PUT, "org/sonatype/foo/maven-metadata.xml", true},
        {SNAPSHOT, PUT, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.sha1", false},
        {RELEASE, PUT, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.sha1", true},
        {MIXED, PUT, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.sha1", true},
        {SNAPSHOT, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.sha1", true},
        {RELEASE, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.sha1", false},
        {MIXED, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.sha1", true},
        {SNAPSHOT, PUT, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.md5", false},
        {RELEASE, PUT, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.md5", true},
        {MIXED, PUT, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.md5", true},
        {SNAPSHOT, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.md5", true},
        {RELEASE, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.md5", false},
        {MIXED, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.md5", true},
        {RELEASE, PUT, "org/sonatype/foo/maven-metadata.xml.sha1", true},
        {MIXED, PUT, "org/sonatype/foo/maven-metadata.xml.sha1", true},
        {SNAPSHOT, PUT, "org/sonatype/foo/maven-metadata.xml.md5", true},
        {RELEASE, PUT, "org/sonatype/foo/maven-metadata.xml.md5", true},
        {MIXED, PUT, "org/sonatype/foo/maven-metadata.xml.md5", true},
        {SNAPSHOT, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.md5", true},
        {RELEASE, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.md5", false},
        {MIXED, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.md5", true},
        {SNAPSHOT, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.sha1", true},
        {RELEASE, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.sha1", false},
        {MIXED, PUT, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.sha1", true},

        // GET should always proceed
        {SNAPSHOT, GET, "org/sonatype/foo/1.0.0/foo-1.0.0.jar", true},
        {RELEASE, GET, "org/sonatype/foo/1.0.0/foo-1.0.0.jar", true},
        {MIXED, GET, "org/sonatype/foo/1.0.0/foo-1.0.0.jar", true},
        {SNAPSHOT, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar", true},
        {RELEASE, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar", true},
        {MIXED, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar", true},
        {SNAPSHOT, GET, "org/sonatype/foo/maven-metadata.xml", true},
        {RELEASE, GET, "org/sonatype/foo/maven-metadata.xml", true},
        {MIXED, GET, "org/sonatype/foo/maven-metadata.xml", true},
        {SNAPSHOT, GET, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.sha1", true},
        {RELEASE, GET, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.sha1", true},
        {MIXED, GET, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.sha1", true},
        {SNAPSHOT, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.sha1", true},
        {RELEASE, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.sha1", true},
        {MIXED, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.sha1", true},
        {SNAPSHOT, GET, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.md5", true},
        {RELEASE, GET, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.md5", true},
        {MIXED, GET, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.md5", true},
        {SNAPSHOT, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.md5", true},
        {RELEASE, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.md5", true},
        {MIXED, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.md5", true},
        {RELEASE, GET, "org/sonatype/foo/maven-metadata.xml.sha1", true},
        {MIXED, GET, "org/sonatype/foo/maven-metadata.xml.sha1", true},
        {SNAPSHOT, GET, "org/sonatype/foo/maven-metadata.xml.md5", true},
        {RELEASE, GET, "org/sonatype/foo/maven-metadata.xml.md5", true},
        {MIXED, GET, "org/sonatype/foo/maven-metadata.xml.md5", true},
        {SNAPSHOT, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.md5", true},
        {RELEASE, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.md5", true},
        {MIXED, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.md5", true},
        {SNAPSHOT, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.sha1", true},
        {RELEASE, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.sha1", true},
        {MIXED, GET, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.sha1", true},

        // HEAD should always proceed
        {SNAPSHOT, HEAD, "org/sonatype/foo/1.0.0/foo-1.0.0.jar", true},
        {RELEASE, HEAD, "org/sonatype/foo/1.0.0/foo-1.0.0.jar", true},
        {MIXED, HEAD, "org/sonatype/foo/1.0.0/foo-1.0.0.jar", true},
        {SNAPSHOT, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar", true},
        {RELEASE, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar", true},
        {MIXED, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar", true},
        {SNAPSHOT, HEAD, "org/sonatype/foo/maven-metadata.xml", true},
        {RELEASE, HEAD, "org/sonatype/foo/maven-metadata.xml", true},
        {MIXED, HEAD, "org/sonatype/foo/maven-metadata.xml", true},
        {SNAPSHOT, HEAD, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.sha1", true},
        {RELEASE, HEAD, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.sha1", true},
        {MIXED, HEAD, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.sha1", true},
        {SNAPSHOT, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.sha1", true},
        {RELEASE, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.sha1", true},
        {MIXED, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.sha1", true},
        {SNAPSHOT, HEAD, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.md5", true},
        {RELEASE, HEAD, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.md5", true},
        {MIXED, HEAD, "org/sonatype/foo/1.0.0/foo-1.0.0.jar.md5", true},
        {SNAPSHOT, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.md5", true},
        {RELEASE, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.md5", true},
        {MIXED, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/foo-1.0.0-20161204.003314-8.jar.md5", true},
        {RELEASE, HEAD, "org/sonatype/foo/maven-metadata.xml.sha1", true},
        {MIXED, HEAD, "org/sonatype/foo/maven-metadata.xml.sha1", true},
        {SNAPSHOT, HEAD, "org/sonatype/foo/maven-metadata.xml.md5", true},
        {RELEASE, HEAD, "org/sonatype/foo/maven-metadata.xml.md5", true},
        {MIXED, HEAD, "org/sonatype/foo/maven-metadata.xml.md5", true},
        {SNAPSHOT, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.md5", true},
        {RELEASE, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.md5", true},
        {MIXED, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.md5", true},
        {SNAPSHOT, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.sha1", true},
        {RELEASE, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.sha1", true},
        {MIXED, HEAD, "org/sonatype/foo/1.0.0-SNAPSHOT/maven-metadata.xml.sha1", true}
    });
  }

  @Parameter
  public VersionPolicy policy;

  @Parameter(1)
  public String httpMethod;

  @Parameter(2)
  public String path;

  @Parameter(3)
  public boolean shouldProceed;

  @Test
  public void testScenario() throws Exception {
    when(context.getRequest()).thenReturn(request);
    when(request.getAction()).thenReturn(httpMethod);
    when(context.getRepository()).thenReturn(repository);
    when(repository.facet(MavenFacet.class)).thenReturn(mavenFacet);
    when(mavenFacet.getVersionPolicy()).thenReturn(policy);
    AttributesMap attributes = new AttributesMap();
    attributes.set(MavenPath.class, mavenPathParser.parsePath(path));
    when(context.getAttributes()).thenReturn(attributes);
    if (shouldProceed) {
      when(context.proceed()).thenReturn(proceeded);
    }

    Response response = underTest.handle(context);
    if (shouldProceed) {
      assertThat(response, is(proceeded));
    }
    else {
      assertThat(response, not(proceeded));
      assertThat(response.getStatus().getCode(), is(BAD_REQUEST));
      assertThat(response.getStatus().isSuccessful(), is(false));
    }
  }
}
