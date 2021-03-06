/*
 * Copyright 2017 The Mifos Initiative.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mifos.individuallending.api.v1.client;

import io.mifos.core.api.util.CustomFeignClientsConfiguration;
import io.mifos.individuallending.api.v1.domain.caseinstance.PlannedPayment;
import io.mifos.individuallending.api.v1.domain.caseinstance.PlannedPaymentPage;
import io.mifos.individuallending.api.v1.domain.product.LossProvisionConfiguration;
import io.mifos.portfolio.api.v1.domain.CasePage;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Stream;

/**
 * @author Myrle Krantz
 */
@SuppressWarnings("unused")
@FeignClient (value = "portfolio-v1", path = "/portfolio/v1", configuration = CustomFeignClientsConfiguration.class)
public interface IndividualLending {

  @RequestMapping(
      value = "/individuallending/products/{productidentifier}/lossprovisionconfiguration",
      method = RequestMethod.PUT,
      produces = MediaType.ALL_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  void changeLossProvisionConfiguration(
      @PathVariable("productidentifier") final String productIdentifier,
      @RequestBody LossProvisionConfiguration lossProvisionConfiguration);

  @RequestMapping(
      value = "/individuallending/products/{productidentifier}/lossprovisionconfiguration",
      method = RequestMethod.GET,
      produces = MediaType.ALL_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  LossProvisionConfiguration getLossProvisionConfiguration(
      @PathVariable("productidentifier") final String productIdentifier);

  @RequestMapping(
          value = "/individuallending/products/{productidentifier}/cases/{caseidentifier}/plannedpayments",
          method = RequestMethod.GET,
          produces = MediaType.ALL_VALUE,
          consumes = MediaType.APPLICATION_JSON_VALUE
  )
  PlannedPaymentPage getPaymentScheduleForCase(
      @PathVariable("productidentifier") final String productIdentifier,
      @PathVariable("caseidentifier") final String caseIdentifier,
      @RequestParam(value = "pageIndex", required = false) final Integer pageIndex,
      @RequestParam(value = "size", required = false) final Integer size,
      @RequestParam(value = "initialDisbursalDate", required = false) final String initialDisbursalDate);

  default Stream<PlannedPayment> getPaymentScheduleForCaseStream(
      final String productIdentifier,
      final String caseIdentifier,
      final String initialDisbursalDate) {
    final PlannedPaymentPage firstPage = this.getPaymentScheduleForCase(
        productIdentifier,
        caseIdentifier,
        0,
        10,
        initialDisbursalDate);

    final Integer pageCount = firstPage.getTotalPages();
        // Sort column is always date and order always ascending so that the order and adjacency of account
        // entries is always stable. This has the advantage that the set of account entries included in the
        // stream is set the moment the first call to fetchAccountEntries (above) is made.
    return Stream.iterate(0, (i) -> i + 1).limit(pageCount)
        .map(i -> this.getPaymentScheduleForCase(productIdentifier, caseIdentifier, i, 10, initialDisbursalDate))
        .flatMap(pageI -> pageI.getElements().stream());
  }

  @RequestMapping(
          value = "/individuallending/customers/{customeridentifier}/cases",
          method = RequestMethod.GET,
          produces = MediaType.ALL_VALUE,
          consumes = MediaType.APPLICATION_JSON_VALUE
  )
  CasePage getAllCasesForCustomer(@PathVariable(value = "customeridentifier") final String customerIdentifier,
                                  @RequestParam("pageIndex") final Integer pageIndex,
                                  @RequestParam("size") final Integer size);
}