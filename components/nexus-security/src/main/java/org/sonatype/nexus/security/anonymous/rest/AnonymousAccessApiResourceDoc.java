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
package org.sonatype.nexus.security.anonymous.rest;

import javax.validation.Valid;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @since 3.next
 */
@Api(value = "Security Management: Anonymous Access")
public interface AnonymousAccessApiResourceDoc
{
  @ApiOperation("Get Anonymous Access settings")
  @ApiResponses(value = {
      @ApiResponse(code = 403, message = "Insufficient permissions to update settings")
  })
  AnonymousAccessSettingsXO read();

  @ApiOperation("Update Anonymous Access settings")
  @ApiResponses(value = {
      @ApiResponse(code = 403, message = "Insufficient permissions to update settings")
  })
  AnonymousAccessSettingsXO update(@Valid AnonymousAccessSettingsXO anonymousXO);
}
