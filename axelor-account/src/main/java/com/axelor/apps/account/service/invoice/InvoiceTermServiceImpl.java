/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.SubstitutePfpValidator;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.InvoiceVisibilityService;
import com.axelor.apps.account.service.PaymentSessionService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.tool.ContextTool;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceTermServiceImpl implements InvoiceTermService {

  protected InvoiceTermRepository invoiceTermRepo;
  protected InvoiceRepository invoiceRepo;
  protected InvoiceService invoiceService;
  protected AppAccountService appAccountService;
  protected InvoiceToolService invoiceToolService;
  protected InvoiceVisibilityService invoiceVisibilityService;
  protected AccountConfigService accountConfigService;
  protected ReconcileService reconcileService;
  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected InvoicePaymentCreateService invoicePaymentCreateService;

  @Inject
  public InvoiceTermServiceImpl(
      InvoiceTermRepository invoiceTermRepo,
      InvoiceRepository invoiceRepo,
      InvoiceService invoiceService,
      AppAccountService appAccountService,
      InvoiceToolService invoiceToolService,
      InvoiceVisibilityService invoiceVisibilityService,
      AccountConfigService accountConfigService,
      ReconcileService reconcileService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoicePaymentCreateService invoicePaymentCreateService) {
    this.invoiceTermRepo = invoiceTermRepo;
    this.invoiceRepo = invoiceRepo;
    this.invoiceService = invoiceService;
    this.appAccountService = appAccountService;
    this.invoiceToolService = invoiceToolService;
    this.invoiceVisibilityService = invoiceVisibilityService;
    this.accountConfigService = accountConfigService;
    this.reconcileService = reconcileService;
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.invoicePaymentCreateService = invoicePaymentCreateService;
  }

  @Override
  public boolean checkInvoiceTermsSum(Invoice invoice) throws AxelorException {

    BigDecimal totalAmount = BigDecimal.ZERO;
    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      totalAmount = totalAmount.add(invoiceTerm.getAmount());
    }
    if (invoice.getInTaxTotal().compareTo(totalAmount) != 0) {
      return false;
    }
    return true;
  }

  @Override
  public boolean checkInvoiceTermsPercentageSum(Invoice invoice) throws AxelorException {

    return new BigDecimal(100).compareTo(computePercentageSum(invoice)) == 0;
  }

  @Override
  public BigDecimal computePercentageSum(Invoice invoice) {

    BigDecimal sum = BigDecimal.ZERO;
    if (CollectionUtils.isNotEmpty(invoice.getInvoiceTermList())) {
      for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
        sum = sum.add(invoiceTerm.getPercentage());
      }
    }
    return sum;
  }

  protected BigDecimal computePercentageSum(MoveLine moveLine) {

    BigDecimal sum = BigDecimal.ZERO;
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      for (InvoiceTerm invoiceTerm : moveLine.getInvoiceTermList()) {
        sum = sum.add(invoiceTerm.getPercentage());
      }
    }
    return sum;
  }

  @Override
  public boolean checkIfCustomizedInvoiceTerms(Invoice invoice) {

    if (!CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
      for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
        if (invoiceTerm.getIsCustomized()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Invoice computeInvoiceTerms(Invoice invoice) throws AxelorException {

    if (invoice.getPaymentCondition() == null
        || CollectionUtils.isEmpty(invoice.getPaymentCondition().getPaymentConditionLineList())) {
      return invoice;
    }

    invoice.clearInvoiceTermList();

    Set<PaymentConditionLine> paymentConditionLines =
        new HashSet<>(invoice.getPaymentCondition().getPaymentConditionLineList());
    Iterator<PaymentConditionLine> iterator = paymentConditionLines.iterator();
    BigDecimal total = BigDecimal.ZERO;
    while (iterator.hasNext()) {
      PaymentConditionLine paymentConditionLine = iterator.next();
      InvoiceTerm invoiceTerm = computeInvoiceTerm(invoice, paymentConditionLine);
      if (!iterator.hasNext()) {
        invoiceTerm.setAmount(invoice.getInTaxTotal().subtract(total));
        invoiceTerm.setAmountRemaining(invoice.getInTaxTotal().subtract(total));
        this.computeAmountRemainingAfterFinDiscount(invoiceTerm);
      } else {
        total = total.add(invoiceTerm.getAmount());
      }
      invoice.addInvoiceTermListItem(invoiceTerm);
    }

    return invoice;
  }

  @Override
  public InvoiceTerm computeInvoiceTerm(Invoice invoice, PaymentConditionLine paymentConditionLine)
      throws AxelorException {

    InvoiceTerm invoiceTerm = new InvoiceTerm();

    invoiceTerm.setPaymentConditionLine(paymentConditionLine);
    BigDecimal amount =
        invoice
            .getInTaxTotal()
            .multiply(paymentConditionLine.getPaymentPercentage())
            .divide(
                BigDecimal.valueOf(100),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP);
    invoiceTerm.setAmount(amount);
    invoiceTerm.setAmountRemaining(amount);

    invoiceTerm.setIsHoldBack(paymentConditionLine.getIsHoldback());
    invoiceTerm.setIsPaid(false);
    invoiceTerm.setPercentage(paymentConditionLine.getPaymentPercentage());

    this.computeFinancialDiscount(invoiceTerm, invoice);

    if (getPfpValidatorUserCondition(invoice)) {
      invoiceTerm.setPfpValidatorUser(invoiceService.getPfpValidatorUser(invoice));
    }
    invoiceTerm.setPaymentMode(invoice.getPaymentMode());
    invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_AWAITING);
    invoiceTerm.setBankDetails(invoice.getBankDetails());
    return invoiceTerm;
  }

  protected void computeFinancialDiscount(InvoiceTerm invoiceTerm, Invoice invoice) {
    this.computeFinancialDiscount(
        invoiceTerm,
        invoice.getFinancialDiscount(),
        invoice.getFinancialDiscountTotalAmount(),
        invoice.getRemainingAmountAfterFinDiscount());
  }

  protected void computeFinancialDiscount(
      InvoiceTerm invoiceTerm,
      FinancialDiscount financialDiscount,
      BigDecimal financialDiscountAmount,
      BigDecimal remainingAmountAfterFinDiscount) {
    if (appAccountService.getAppAccount().getManageFinancialDiscount()) {
      BigDecimal percentage =
          invoiceTerm.getPercentage().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

      invoiceTerm.setApplyFinancialDiscount(financialDiscount != null);
      invoiceTerm.setFinancialDiscount(financialDiscount);
      invoiceTerm.setFinancialDiscountAmount(
          financialDiscountAmount.multiply(percentage).setScale(2, RoundingMode.HALF_UP));
      invoiceTerm.setRemainingAmountAfterFinDiscount(
          remainingAmountAfterFinDiscount.multiply(percentage).setScale(2, RoundingMode.HALF_UP));
      this.computeAmountRemainingAfterFinDiscount(invoiceTerm);

      invoiceTerm.setFinancialDiscountDeadlineDate(
          this.computeFinancialDiscountDeadlineDate(invoiceTerm));
    }
  }

  protected void computeAmountRemainingAfterFinDiscount(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getAmount().signum() > 0) {
      invoiceTerm.setAmountRemainingAfterFinDiscount(
          invoiceTerm
              .getAmountRemaining()
              .multiply(invoiceTerm.getRemainingAmountAfterFinDiscount())
              .divide(invoiceTerm.getAmount(), 2, RoundingMode.HALF_UP));
    }
  }

  protected boolean getPfpValidatorUserCondition(Invoice invoice) {
    return appAccountService.getAppAccount().getActivatePassedForPayment()
        && (invoice.getCompany().getAccountConfig().getIsManagePassedForPayment()
            && (invoice.getOperationTypeSelect()
                    == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
                || (invoice.getCompany().getAccountConfig().getIsManagePFPInRefund()
                    && invoice.getOperationTypeSelect()
                        == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND)));
  }

  @Override
  public InvoiceTerm initCustomizedInvoiceTerm(Invoice invoice, InvoiceTerm invoiceTerm) {

    invoiceTerm.setInvoice(invoice);
    invoiceTerm.setIsCustomized(true);
    invoiceTerm.setIsPaid(false);
    invoiceTerm.setIsHoldBack(false);
    invoiceTerm.setPaymentMode(invoice.getPaymentMode());

    BigDecimal invoiceTermPercentage = BigDecimal.ZERO;
    BigDecimal percentageSum = computePercentageSum(invoice);
    if (percentageSum.compareTo(BigDecimal.ZERO) > 0) {
      invoiceTermPercentage = new BigDecimal(100).subtract(percentageSum);
    }
    invoiceTerm.setPercentage(invoiceTermPercentage);
    BigDecimal amount =
        invoice
            .getInTaxTotal()
            .multiply(invoiceTermPercentage)
            .divide(
                BigDecimal.valueOf(100),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP);
    invoiceTerm.setAmount(amount);
    invoiceTerm.setAmountRemaining(amount);
    this.computeFinancialDiscount(invoiceTerm, invoice);

    if (invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED) {
      MoveLine moveLine = getExistingInvoiceTermMoveLine(invoice);
      moveLine.addInvoiceTermListItem(invoiceTerm);
    }

    return invoiceTerm;
  }

  @Override
  public InvoiceTerm initCustomizedInvoiceTerm(
      MoveLine moveLine, InvoiceTerm invoiceTerm, Move move) {

    invoiceTerm.setInvoice(move.getInvoice());
    invoiceTerm.setSequence(initInvoiceTermsSequence(moveLine));

    invoiceTerm.setIsCustomized(true);
    invoiceTerm.setIsPaid(false);
    invoiceTerm.setIsHoldBack(false);
    BigDecimal invoiceTermPercentage = BigDecimal.ZERO;
    BigDecimal percentageSum = computePercentageSum(moveLine);
    if (percentageSum.compareTo(BigDecimal.ZERO) > 0) {
      invoiceTermPercentage = new BigDecimal(100).subtract(percentageSum);
    }
    invoiceTerm.setPercentage(invoiceTermPercentage);
    BigDecimal amount;
    if (moveLine.getCredit().compareTo(moveLine.getDebit()) <= 0) {
      amount = moveLine.getDebit();
    } else {
      amount = moveLine.getCredit();
    }
    amount =
        amount
            .multiply(invoiceTermPercentage)
            .divide(
                BigDecimal.valueOf(100),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP);
    invoiceTerm.setAmount(amount);
    invoiceTerm.setAmountRemaining(amount);

    return invoiceTerm;
  }

  @Override
  public MoveLine getExistingInvoiceTermMoveLine(Invoice invoice) {

    InvoiceTerm invoiceTerm =
        invoiceTermRepo
            .all()
            .filter("self.invoice.id = ?1 AND self.isHoldBack is not true", invoice.getId())
            .fetchOne();
    if (invoiceTerm == null) {
      return null;
    } else {
      return invoiceTerm.getMoveLine();
    }
  }

  @Override
  public Invoice setDueDates(Invoice invoice, LocalDate invoiceDate) {

    if (invoice.getPaymentCondition() == null
        || CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
      return invoice;
    }

    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      if (!invoiceTerm.getIsCustomized()) {
        LocalDate dueDate =
            InvoiceToolService.getDueDate(invoiceTerm.getPaymentConditionLine(), invoiceDate);
        invoiceTerm.setDueDate(dueDate);

        if (appAccountService.getAppAccount().getManageFinancialDiscount()
            && invoiceTerm.getApplyFinancialDiscount()
            && invoiceTerm.getFinancialDiscount() != null) {
          invoiceTerm.setFinancialDiscountDeadlineDate(
              this.computeFinancialDiscountDeadlineDate(invoiceTerm));
        }
      }
    }

    initInvoiceTermsSequence(invoice);
    return invoice;
  }

  protected LocalDate computeFinancialDiscountDeadlineDate(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getDueDate() == null || invoiceTerm.getFinancialDiscount() == null) {
      return null;
    }

    LocalDate deadlineDate =
        invoiceTerm.getDueDate().minusDays(invoiceTerm.getFinancialDiscount().getDiscountDelay());

    if (invoiceTerm.getInvoice() != null && invoiceTerm.getInvoice().getInvoiceDate() != null) {
      LocalDate invoiceDate = invoiceTerm.getInvoice().getInvoiceDate();
      deadlineDate = deadlineDate.isBefore(invoiceDate) ? invoiceDate : deadlineDate;
    }

    return deadlineDate;
  }

  @Override
  public void initInvoiceTermsSequence(Invoice invoice) {

    invoice.getInvoiceTermList().sort(Comparator.comparing(InvoiceTerm::getDueDate));
    int sequence = 1;
    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      invoiceTerm.setSequence(sequence);
      sequence++;
    }
  }

  protected int initInvoiceTermsSequence(MoveLine moveLine) {
    if (CollectionUtils.isEmpty(moveLine.getInvoiceTermList())) {
      return 1;
    }
    return moveLine.getInvoiceTermList().stream()
            .max(Comparator.comparing(InvoiceTerm::getSequence))
            .get()
            .getSequence()
        + 1;
  }

  @Override
  public List<InvoiceTerm> getUnpaidInvoiceTerms(Invoice invoice) {
    String queryStr = "self.invoice = :invoice AND self.isPaid IS NOT TRUE";
    boolean pfpCondition =
        appAccountService.getAppAccount().getActivatePassedForPayment()
            && invoiceVisibilityService.getManagePfpCondition(invoice)
            && invoiceVisibilityService.getOperationTypePurchaseCondition(invoice);

    if (pfpCondition) {
      queryStr =
          queryStr + " AND self.pfpValidateStatusSelect IN (:validated, :partiallyValidated)";
    }

    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo.all().filter(queryStr).bind("invoice", invoice);

    if (pfpCondition) {
      invoiceTermQuery
          .bind("validated", InvoiceTermRepository.PFP_STATUS_VALIDATED)
          .bind("partiallyValidated", InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED);
    }

    return invoiceTermQuery.order("dueDate").fetch();
  }

  @Override
  public List<InvoiceTerm> filterInvoiceTermsByHoldBack(List<InvoiceTerm> invoiceTerms) {

    if (CollectionUtils.isEmpty(invoiceTerms)) {
      return invoiceTerms;
    }

    boolean isFirstHoldBack = invoiceTerms.get(0).getIsHoldBack();
    invoiceTerms.removeIf(it -> it.getIsHoldBack() != isFirstHoldBack);

    return invoiceTerms;
  }

  @Override
  public List<InvoiceTerm> getUnpaidInvoiceTermsFiltered(Invoice invoice) {

    return filterInvoiceTermsByHoldBack(getUnpaidInvoiceTerms(invoice));
  }

  @Override
  public LocalDate getLatestInvoiceTermDueDate(Invoice invoice) {

    List<InvoiceTerm> invoiceTerms = invoice.getInvoiceTermList();
    if (CollectionUtils.isEmpty(invoiceTerms)) {
      return invoice.getInvoiceDate();
    }
    LocalDate dueDate = null;
    for (InvoiceTerm invoiceTerm : invoiceTerms) {
      if (!invoiceTerm.getIsHoldBack()
          && (dueDate == null || dueDate.isBefore(invoiceTerm.getDueDate()))) {
        dueDate = invoiceTerm.getDueDate();
      }
    }
    return dueDate;
  }

  @Override
  public void updateInvoiceTermsPaidAmount(InvoicePayment invoicePayment) throws AxelorException {

    if (CollectionUtils.isEmpty(invoicePayment.getInvoiceTermPaymentList())) {
      return;
    }

    this.updateInvoiceTermsPaidAmount(
        invoicePayment.getInvoiceTermPaymentList(), invoicePayment.getPaymentMode());
  }

  @Override
  public void updateInvoiceTermsPaidAmount(
      InvoicePayment invoicePayment,
      InvoiceTerm invoiceTermToPay,
      InvoiceTermPayment invoiceTermPayment)
      throws AxelorException {
    this.updateInvoiceTermsPaidAmount(
        Collections.singletonList(invoiceTermPayment), invoiceTermToPay.getPaymentMode());
  }

  protected void updateInvoiceTermsPaidAmount(
      List<InvoiceTermPayment> invoiceTermPaymentList, PaymentMode paymentMode)
      throws AxelorException {
    for (InvoiceTermPayment invoiceTermPayment : invoiceTermPaymentList) {
      InvoiceTerm invoiceTerm = invoiceTermPayment.getInvoiceTerm();
      BigDecimal paidAmount =
          invoiceTermPayment.getPaidAmount().add(invoiceTermPayment.getFinancialDiscountAmount());

      BigDecimal amountRemaining = invoiceTerm.getAmountRemaining().subtract(paidAmount);
      invoiceTerm.setPaymentMode(paymentMode);

      if (amountRemaining.signum() <= 0) {
        amountRemaining = BigDecimal.ZERO;
        invoiceTerm.setIsPaid(true);
        Invoice invoice = invoiceTerm.getInvoice();
        if (invoice != null) {
          invoice.setDueDate(InvoiceToolService.getDueDate(invoice));
        }
      }
      invoiceTerm.setAmountRemaining(amountRemaining);
      this.computeAmountRemainingAfterFinDiscount(invoiceTerm);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateInvoiceTermsAmountRemaining(InvoicePayment invoicePayment)
      throws AxelorException {
    this.updateInvoiceTermsAmountRemaining(invoicePayment.getInvoiceTermPaymentList());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateInvoiceTermsAmountRemaining(List<InvoiceTermPayment> invoiceTermPaymentList)
      throws AxelorException {

    for (InvoiceTermPayment invoiceTermPayment : invoiceTermPaymentList) {
      InvoiceTerm invoiceTerm = invoiceTermPayment.getInvoiceTerm();
      BigDecimal paidAmount =
          invoiceTermPayment.getPaidAmount().add(invoiceTermPayment.getFinancialDiscountAmount());
      invoiceTerm.setAmountRemaining(invoiceTerm.getAmountRemaining().add(paidAmount));
      this.computeAmountRemainingAfterFinDiscount(invoiceTerm);
      if (invoiceTerm.getAmountRemaining().signum() > 0) {
        invoiceTerm.setIsPaid(false);
        Invoice invoice = invoiceTerm.getInvoice();
        if (invoice != null) {
          invoice.setDueDate(InvoiceToolService.getDueDate(invoice));
        }
        invoiceTermRepo.save(invoiceTerm);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateFinancialDiscount(Invoice invoice) {
    invoice.getInvoiceTermList().stream()
        .filter(it -> it.getAmountRemaining().compareTo(it.getAmount()) == 0)
        .forEach(it -> this.computeFinancialDiscount(it, invoice));

    invoiceRepo.save(invoice);
  }

  @Override
  public boolean checkInvoiceTermCreationConditions(Invoice invoice) {

    if (invoice.getId() == null
        || ObjectUtils.isEmpty(invoice.getInvoiceTermList())
        || (invoice.getInTaxTotal().signum() == 0
            && invoice.getStatusSelect() == InvoiceRepository.STATUS_DRAFT
            && !ObjectUtils.isEmpty(invoice.getInvoiceLineList()))
        || ObjectUtils.isEmpty(invoice.getInvoiceLineList())
        || invoice.getAmountRemaining().signum() > 0) {
      return false;
    }
    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      if (invoiceTerm.getId() == null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean checkIfThereIsDeletedHoldbackInvoiceTerms(Invoice invoice) {

    if (invoice.getId() == null) {

      return false;
    }
    if (invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED) {

      List<InvoiceTerm> invoiceTermWithHoldback =
          invoiceTermRepo
              .all()
              .filter("self.invoice.id = ?1 AND self.isHoldBack is true", invoice.getId())
              .fetch();

      if (CollectionUtils.isEmpty(invoiceTermWithHoldback)) {
        return false;
      }
      List<InvoiceTerm> invoiceTerms = invoice.getInvoiceTermList();

      for (InvoiceTerm persistedInvoiceTermWithHoldback : invoiceTermWithHoldback) {
        if (!invoiceTerms.contains(persistedInvoiceTermWithHoldback)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean checkInvoiceTermDeletionConditions(Invoice invoice) {

    if (invoice.getId() == null || invoice.getAmountPaid().compareTo(BigDecimal.ZERO) == 0) {
      return false;
    }

    Invoice persistedInvoice = invoiceRepo.find(invoice.getId());

    if (CollectionUtils.isEmpty(persistedInvoice.getInvoiceTermList())) {
      return false;

    } else {

      List<InvoiceTerm> invoiceTerms = invoice.getInvoiceTermList();
      if (CollectionUtils.isEmpty(invoiceTerms)) {
        return true;
      }
      for (InvoiceTerm persistedInvoiceTerm : persistedInvoice.getInvoiceTermList()) {
        if (!invoiceTerms.contains(persistedInvoiceTerm)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void refusalToPay(
      InvoiceTerm invoiceTerm, CancelReason reasonOfRefusalToPay, String reasonOfRefusalToPayStr) {
    invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_LITIGATION);
    invoiceTerm.setDecisionPfpTakenDate(
        Beans.get(AppBaseService.class).getTodayDate(invoiceTerm.getInvoice().getCompany()));
    invoiceTerm.setPfpGrantedAmount(BigDecimal.ZERO);
    invoiceTerm.setPfpRejectedAmount(invoiceTerm.getAmount());
    invoiceTerm.setPfpValidatorUser(AuthUtils.getUser());
    invoiceTerm.setReasonOfRefusalToPay(reasonOfRefusalToPay);
    invoiceTerm.setReasonOfRefusalToPayStr(
        reasonOfRefusalToPayStr != null ? reasonOfRefusalToPayStr : reasonOfRefusalToPay.getName());

    invoiceTermRepo.save(invoiceTerm);
  }

  @Override
  @Transactional
  public void retrieveEligibleTerms(PaymentSession paymentSession) {
    List<InvoiceTerm> eligibleInvoiceTermList =
        invoiceTermRepo
            .all()
            .filter(retrieveEligibleTermsQuery())
            .bind("company", paymentSession.getCompany())
            .bind("paymentModeTypeSelect", paymentSession.getPaymentMode().getTypeSelect())
            .bind(
                "paymentDatePlusMargin",
                paymentSession
                    .getPaymentDate()
                    .plusDays(paymentSession.getDaysMarginOnPaySession()))
            .bind("currency", paymentSession.getCurrency())
            .bind("partnerTypeSelect", paymentSession.getPartnerTypeSelect())
            .bind("receivable", AccountTypeRepository.TYPE_RECEIVABLE)
            .bind("payable", AccountTypeRepository.TYPE_PAYABLE)
            .bind("partnerTypeClient", PaymentSessionRepository.PARTNER_TYPE_CUSTOMER)
            .bind("partnerTypeSupplier", PaymentSessionRepository.PARTNER_TYPE_SUPPLIER)
            .bind("functionalOriginClient", MoveRepository.FUNCTIONAL_ORIGIN_SALE)
            .bind("functionalOriginSupplier", MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE)
            .bind("pfpValidateStatusValidated", InvoiceTermRepository.PFP_STATUS_VALIDATED)
            .bind(
                "pfpValidateStatusPartiallyValidated",
                InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED)
            .fetch();
    eligibleInvoiceTermList.forEach(
        invoiceTerm -> {
          fillEligibleTerm(paymentSession, invoiceTerm);
          invoiceTermRepo.save(invoiceTerm);
        });
    Beans.get(PaymentSessionService.class).computeTotalPaymentSession(paymentSession);
  }

  protected String retrieveEligibleTermsQuery() {
    String generalCondition =
        "self.moveLine.move.company = :company "
            + " AND self.dueDate <= :paymentDatePlusMargin "
            + " AND self.moveLine.move.currency = :currency "
            + " AND self.bankDetails IS NOT NULL "
            + " AND self.paymentMode.typeSelect = :paymentModeTypeSelect"
            + " AND self.moveLine.account.isRetrievedOnPaymentSession = TRUE ";
    String termsMoveLineCondition =
        " AND ((self.moveLine.partner.isCustomer = TRUE "
            + " AND :partnerTypeSelect = :partnerTypeClient"
            + " AND self.moveLine.move.functionalOriginSelect = :functionalOriginClient)"
            + " OR ( self.moveLine.partner.isSupplier = TRUE "
            + " AND :partnerTypeSelect = :partnerTypeSupplier "
            + " AND self.moveLine.move.functionalOriginSelect = :functionalOriginSupplier "
            + " AND (self.moveLine.move.company.accountConfig.isManagePassedForPayment is NULL "
            + " OR self.moveLine.move.company.accountConfig.isManagePassedForPayment = FALSE  "
            + " OR (self.moveLine.move.company.accountConfig.isManagePassedForPayment = TRUE "
            + " AND (self.pfpValidateStatusSelect = :pfpValidateStatusValidated OR self.pfpValidateStatusSelect = :pfpValidateStatusPartiallyValidated))))) ";
    String paymentHistoryCondition =
        " AND self.isPaid = FALSE"
            + " AND self.amountRemaining > 0"
            + " AND self.paymentSession IS NULL";
    return generalCondition + termsMoveLineCondition + paymentHistoryCondition;
  }

  protected void fillEligibleTerm(PaymentSession paymentSession, InvoiceTerm invoiceTerm) {
    LocalDate nextSessionDate = paymentSession.getNextSessionDate();
    LocalDate paymentDate = paymentSession.getPaymentDate();
    LocalDate financialDiscountDeadlineDate = invoiceTerm.getFinancialDiscountDeadlineDate();
    boolean isSignedNegative = this.getIsSignedNegative(invoiceTerm);

    invoiceTerm.setPaymentSession(paymentSession);
    invoiceTerm.setIsSelectedOnPaymentSession(true);
    if (isSignedNegative) {
      invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining().negate());

    } else {
      invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining());
    }

    if (invoiceTerm.getApplyFinancialDiscount() && financialDiscountDeadlineDate != null) {
      if (invoiceTerm.getFinancialDiscountAmount().compareTo(invoiceTerm.getAmountRemaining())
          > 0) {
        invoiceTerm.setApplyFinancialDiscountOnPaymentSession(false);
      } else if (paymentDate != null && !financialDiscountDeadlineDate.isBefore(paymentDate)) {
        invoiceTerm.setApplyFinancialDiscountOnPaymentSession(true);
      }
      if (nextSessionDate != null && !financialDiscountDeadlineDate.isBefore(nextSessionDate)) {
        invoiceTerm.setIsSelectedOnPaymentSession(false);
      }
    }

    computeAmountPaid(invoiceTerm);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validatePfp(InvoiceTerm invoiceTerm, User currentUser) {
    invoiceTerm.setDecisionPfpTakenDate(
        Beans.get(AppBaseService.class).getTodayDate(invoiceTerm.getInvoice().getCompany()));
    invoiceTerm.setPfpGrantedAmount(invoiceTerm.getAmount());
    invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_VALIDATED);
    invoiceTerm.setPfpValidatorUser(currentUser);
    invoiceTermRepo.save(invoiceTerm);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Integer massValidatePfp(List<Long> invoiceTermIds) {
    List<InvoiceTerm> invoiceTermList =
        invoiceTermRepo
            .all()
            .filter(
                "self.id in ? AND self.pfpValidateStatusSelect != ?",
                invoiceTermIds,
                InvoiceTermRepository.PFP_STATUS_VALIDATED)
            .fetch();
    User currentUser = AuthUtils.getUser();
    int updatedRecords = 0;
    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      if (canUpdateInvoiceTerm(invoiceTerm, currentUser)) {
        validatePfp(invoiceTerm, currentUser);
        updatedRecords++;
      }
    }
    return updatedRecords;
  }

  @Override
  public Integer massRefusePfp(
      List<Long> invoiceTermIds,
      CancelReason reasonOfRefusalToPay,
      String reasonOfRefusalToPayStr) {
    List<InvoiceTerm> invoiceTermList =
        invoiceTermRepo
            .all()
            .filter(
                "self.id in ? AND self.pfpValidateStatusSelect != ?",
                invoiceTermIds,
                InvoiceTermRepository.PFP_STATUS_LITIGATION)
            .fetch();
    User currentUser = AuthUtils.getUser();
    int updatedRecords = 0;
    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      boolean invoiceTermCheck =
          ObjectUtils.notEmpty(invoiceTerm.getInvoice())
              && ObjectUtils.notEmpty(invoiceTerm.getInvoice().getCompany())
              && ObjectUtils.notEmpty(reasonOfRefusalToPay);
      if (invoiceTermCheck && canUpdateInvoiceTerm(invoiceTerm, currentUser)) {
        refusalToPay(invoiceTerm, reasonOfRefusalToPay, reasonOfRefusalToPayStr);
        updatedRecords++;
      }
    }
    return updatedRecords;
  }

  @Override
  public BigDecimal getFinancialDiscountTaxAmount(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() != null
        && invoiceTerm.getFinancialDiscount() != null
        && invoiceTerm.getFinancialDiscount().getDiscountBaseSelect()
            == FinancialDiscountRepository.DISCOUNT_BASE_VAT) {
      return invoiceTerm
          .getInvoice()
          .getTaxTotal()
          .multiply(invoiceTerm.getPercentage())
          .multiply(invoiceTerm.getFinancialDiscount().getDiscountRate())
          .divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);
    } else {
      return BigDecimal.ZERO;
    }
  }

  @Override
  public BigDecimal getAmountRemaining(InvoiceTerm invoiceTerm, LocalDate date) {
    return invoiceTerm.getApplyFinancialDiscount()
            && invoiceTerm.getFinancialDiscountDeadlineDate() != null
            && !invoiceTerm.getFinancialDiscountDeadlineDate().isBefore(date)
        ? invoiceTerm.getAmountRemainingAfterFinDiscount()
        : invoiceTerm.getAmountRemaining();
  }

  protected boolean canUpdateInvoiceTerm(InvoiceTerm invoiceTerm, User currentUser) {
    boolean isValidUser =
        currentUser.getIsSuperPfpUser()
            || (ObjectUtils.notEmpty(invoiceTerm.getPfpValidatorUser())
                && currentUser.equals(invoiceTerm.getPfpValidatorUser()));
    if (isValidUser) {
      return true;
    }
    return validateUser(invoiceTerm, currentUser)
        && (ObjectUtils.notEmpty(invoiceTerm.getPfpValidatorUser())
            && invoiceTerm
                .getPfpValidatorUser()
                .equals(invoiceService.getPfpValidatorUser(invoiceTerm.getInvoice())))
        && !invoiceTerm.getIsPaid();
  }

  protected boolean validateUser(InvoiceTerm invoiceTerm, User currentUser) {
    boolean isValidUser = false;
    if (ObjectUtils.notEmpty(invoiceTerm.getPfpValidatorUser())) {
      List<SubstitutePfpValidator> substitutePfpValidatorList =
          invoiceTerm.getPfpValidatorUser().getSubstitutePfpValidatorList();
      LocalDate todayDate =
          Beans.get(AppBaseService.class).getTodayDate(invoiceTerm.getInvoice().getCompany());

      for (SubstitutePfpValidator substitutePfpValidator : substitutePfpValidatorList) {
        if (substitutePfpValidator.getSubstitutePfpValidatorUser().equals(currentUser)) {
          LocalDate substituteStartDate = substitutePfpValidator.getSubstituteStartDate();
          LocalDate substituteEndDate = substitutePfpValidator.getSubstituteEndDate();
          if (substituteStartDate == null) {
            if (substituteEndDate == null || substituteEndDate.isAfter(todayDate)) {
              isValidUser = true;
              break;
            }
          } else {
            if (substituteEndDate == null && substituteStartDate.isBefore(todayDate)) {
              isValidUser = true;
              break;
            } else if (substituteStartDate.isBefore(todayDate)
                && substituteEndDate.isAfter(todayDate)) {
              isValidUser = true;
              break;
            }
          }
        }
      }
    }
    return isValidUser;
  }

  @Override
  public BigDecimal computeCustomizedPercentage(BigDecimal amount, BigDecimal inTaxTotal) {
    BigDecimal percentage = BigDecimal.ZERO;
    if (inTaxTotal.compareTo(BigDecimal.ZERO) != 0) {
      percentage =
          amount
              .multiply(new BigDecimal(100))
              .divide(inTaxTotal, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    }
    return percentage;
  }

  @Override
  @Transactional
  public void generateInvoiceTerm(
      InvoiceTerm originalInvoiceTerm,
      BigDecimal invoiceAmount,
      BigDecimal pfpGrantedAmount,
      PfpPartialReason partialReason) {
    BigDecimal amount = invoiceAmount.subtract(pfpGrantedAmount);
    Invoice invoice = originalInvoiceTerm.getInvoice();
    createInvoiceTerm(originalInvoiceTerm, invoice, amount);
    updateOriginalTerm(originalInvoiceTerm, pfpGrantedAmount, partialReason, amount, invoice);

    initInvoiceTermsSequence(originalInvoiceTerm.getInvoice());
  }

  @Transactional
  protected InvoiceTerm createInvoiceTerm(
      InvoiceTerm originalInvoiceTerm, Invoice invoice, BigDecimal amount) {
    return invoiceTermRepo.save(
        this.createInvoiceTerm(
            invoice,
            originalInvoiceTerm.getMoveLine(),
            originalInvoiceTerm.getBankDetails(),
            originalInvoiceTerm.getPfpValidatorUser(),
            originalInvoiceTerm.getPaymentMode(),
            originalInvoiceTerm.getDueDate(),
            originalInvoiceTerm.getEstimatedPaymentDate(),
            amount,
            computeCustomizedPercentage(amount, invoice.getInTaxTotal()),
            originalInvoiceTerm.getIsHoldBack()));
  }

  @Override
  public InvoiceTerm createInvoiceTerm(
      MoveLine moveLine,
      BankDetails bankDetails,
      User pfpUser,
      PaymentMode paymentMode,
      LocalDate date,
      BigDecimal amount) {
    return this.createInvoiceTerm(
        null,
        moveLine,
        bankDetails,
        pfpUser,
        paymentMode,
        date,
        null,
        amount,
        BigDecimal.valueOf(100),
        false);
  }

  @Override
  public InvoiceTerm createInvoiceTerm(
      Invoice invoice,
      MoveLine moveLine,
      BankDetails bankDetails,
      User pfpUser,
      PaymentMode paymentMode,
      LocalDate date,
      LocalDate estimatedPaymentDate,
      BigDecimal amount,
      BigDecimal percentage,
      boolean isHoldBack) {
    InvoiceTerm newInvoiceTerm = new InvoiceTerm();

    newInvoiceTerm.setInvoice(invoice);
    newInvoiceTerm.setIsCustomized(true);
    newInvoiceTerm.setIsPaid(false);
    newInvoiceTerm.setDueDate(date);
    newInvoiceTerm.setIsHoldBack(isHoldBack);
    newInvoiceTerm.setEstimatedPaymentDate(estimatedPaymentDate);
    newInvoiceTerm.setAmount(amount);
    newInvoiceTerm.setAmountRemaining(amount);
    newInvoiceTerm.setPaymentMode(paymentMode);
    newInvoiceTerm.setBankDetails(bankDetails);
    newInvoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_AWAITING);
    newInvoiceTerm.setPfpValidatorUser(pfpUser);
    newInvoiceTerm.setPfpGrantedAmount(BigDecimal.ZERO);
    newInvoiceTerm.setPfpRejectedAmount(BigDecimal.ZERO);
    newInvoiceTerm.setPercentage(percentage);

    moveLine.addInvoiceTermListItem(newInvoiceTerm);

    return newInvoiceTerm;
  }

  @Transactional
  protected void updateOriginalTerm(
      InvoiceTerm originalInvoiceTerm,
      BigDecimal pfpGrantedAmount,
      PfpPartialReason partialReason,
      BigDecimal amount,
      Invoice invoice) {
    originalInvoiceTerm.setIsCustomized(true);
    originalInvoiceTerm.setIsPaid(false);
    originalInvoiceTerm.setAmount(pfpGrantedAmount);
    originalInvoiceTerm.setPercentage(
        computeCustomizedPercentage(pfpGrantedAmount, invoice.getInTaxTotal()));
    originalInvoiceTerm.setAmountRemaining(pfpGrantedAmount);
    originalInvoiceTerm.setPfpValidateStatusSelect(
        InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED);
    originalInvoiceTerm.setPfpGrantedAmount(pfpGrantedAmount);
    originalInvoiceTerm.setPfpRejectedAmount(amount);
    originalInvoiceTerm.setDecisionPfpTakenDate(LocalDate.now());
    originalInvoiceTerm.setPfpPartialReason(partialReason);
  }

  public void managePassedForPayment(InvoiceTerm invoiceTerm) throws AxelorException {
    if (invoiceTerm.getInvoice() != null && invoiceTerm.getInvoice().getCompany() != null) {
      boolean isSignedNegative = this.getIsSignedNegative(invoiceTerm);
      if (accountConfigService
              .getAccountConfig(invoiceTerm.getInvoice().getCompany())
              .getIsManagePassedForPayment()
          && invoiceTerm.getInvoice().getOperationTypeSelect()
              == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE) {
        if (isSignedNegative) {
          invoiceTerm.setPaymentAmount(invoiceTerm.getPfpGrantedAmount().negate());
        } else {
          invoiceTerm.setPaymentAmount(invoiceTerm.getPfpGrantedAmount());
        }
      } else {
        if (isSignedNegative) {
          invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining().negate());
        } else {
          invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining());
        }
      }
    }
  }

  @Override
  @Transactional
  public void toggle(InvoiceTerm invoiceTerm, boolean value) throws AxelorException {
    if (invoiceTerm != null) {
      invoiceTerm.setIsSelectedOnPaymentSession(value);
      managePassedForPayment(invoiceTerm);
      computeAmountPaid(invoiceTerm);
      invoiceTermRepo.save(invoiceTerm);
    }
  }

  @Override
  public void computeAmountPaid(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getIsSelectedOnPaymentSession()) {
      if (invoiceTerm.getApplyFinancialDiscountOnPaymentSession()) {
        BigDecimal financialDiscountAmount =
            invoiceTerm.getPaymentAmount().compareTo(BigDecimal.ZERO) < 0
                ? invoiceTerm.getFinancialDiscountAmount()
                : invoiceTerm.getFinancialDiscountAmount().negate();
        invoiceTerm.setAmountPaid(invoiceTerm.getPaymentAmount().add(financialDiscountAmount));
      } else {
        invoiceTerm.setAmountPaid(invoiceTerm.getPaymentAmount());
      }
    } else {
      invoiceTerm.setAmountPaid(BigDecimal.ZERO);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void reconcileAndUpdateInvoiceTermsAmounts(
      InvoiceTerm invoiceTermFromInvoice, InvoiceTerm invoiceTermFromRefund)
      throws AxelorException {
    BigDecimal reconciledAmount =
        invoiceTermFromInvoice.getAmountRemaining().min(invoiceTermFromRefund.getAmountRemaining());

    MoveLine creditMoveLine = null;
    MoveLine debitMoveLine = null;
    if (invoiceTermFromInvoice.getMoveLine().getMove().getFunctionalOriginSelect()
        == MoveRepository.FUNCTIONAL_ORIGIN_SALE) {
      creditMoveLine = invoiceTermFromRefund.getMoveLine();
      debitMoveLine = invoiceTermFromInvoice.getMoveLine();
    } else if (invoiceTermFromInvoice.getMoveLine().getMove().getFunctionalOriginSelect()
        == MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE) {
      creditMoveLine = invoiceTermFromInvoice.getMoveLine();
      debitMoveLine = invoiceTermFromRefund.getMoveLine();
    }
    Reconcile invoiceTermsReconcile =
        reconcileService.createReconcile(debitMoveLine, creditMoveLine, reconciledAmount, true);

    reconcileService.confirmReconcile(invoiceTermsReconcile, false, false);

    updateInvoiceTermsAmounts(
        invoiceTermFromInvoice,
        reconciledAmount,
        invoiceTermsReconcile,
        invoiceTermFromRefund.getMoveLine().getMove());
    updateInvoiceTermsAmounts(
        invoiceTermFromRefund,
        reconciledAmount,
        invoiceTermsReconcile,
        invoiceTermFromInvoice.getMoveLine().getMove());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<InvoiceTerm> reconcileMoveLineInvoiceTermsWithFullRollBack(
      List<InvoiceTerm> invoiceTermList) throws AxelorException {
    List<Partner> partnerList = getPartnersFromInvoiceTermList(invoiceTermList);

    for (Partner partner : partnerList) {

      List<InvoiceTerm> invoiceTermFromInvoiceList =
          getInvoiceTermsInvoiceOrRefundSortedByDueDateAndByPartner(invoiceTermList, partner, true);
      List<InvoiceTerm> invoiceTermFromRefundList =
          getInvoiceTermsInvoiceOrRefundSortedByDueDateAndByPartner(
              invoiceTermList, partner, false);
      int invoiceCounter = 0;
      int refundCounter = 0;
      InvoiceTerm invoiceTermFromInvoice = null;
      InvoiceTerm invoiceTermFromRefund = null;
      while (!ObjectUtils.isEmpty(invoiceTermFromRefundList)
          && !ObjectUtils.isEmpty(invoiceTermFromInvoiceList)
          && invoiceCounter < invoiceTermFromInvoiceList.size()
          && refundCounter < invoiceTermFromRefundList.size()) {
        invoiceTermFromInvoice = invoiceTermFromInvoiceList.get(invoiceCounter);
        invoiceTermFromRefund = invoiceTermFromRefundList.get(refundCounter);
        this.reconcileAndUpdateInvoiceTermsAmounts(invoiceTermFromInvoice, invoiceTermFromRefund);
        if (invoiceTermFromInvoice.getAmountRemaining().signum() == 0) {
          invoiceTermFromInvoice.setIsPaid(true);
          invoiceCounter++;
        }
        if (invoiceTermFromRefund.getAmountRemaining().signum() == 0) {
          invoiceTermFromRefund.setIsPaid(true);
          refundCounter++;
        }
      }
    }
    return invoiceTermList;
  }

  protected List<Partner> getPartnersFromInvoiceTermList(List<InvoiceTerm> invoiceTermList) {
    return invoiceTermList.stream()
        .map(it -> it.getMoveLine().getPartner())
        .distinct()
        .collect(Collectors.toList());
  }

  protected List<InvoiceTerm> getInvoiceTermsInvoiceOrRefundSortedByDueDateAndByPartner(
      List<InvoiceTerm> invoiceTermList, Partner partner, boolean isInvoice) {
    return invoiceTermList.stream()
        .filter(
            it ->
                ((it.getAmountPaid().signum() > 0 && isInvoice)
                        || (it.getAmountPaid().signum() < 0 && !isInvoice))
                    && it.getMoveLine().getPartner().equals(partner))
        .sorted(Comparator.comparing(InvoiceTerm::getDueDate))
        .collect(Collectors.toList());
  }

  protected InvoiceTerm updateInvoiceTermsAmounts(
      InvoiceTerm invoiceTerm, BigDecimal amount, Reconcile reconcile, Move move)
      throws AxelorException {

    InvoicePayment invoicePayment =
        invoicePaymentCreateService.createInvoicePayment(invoiceTerm.getInvoice(), amount, move);
    invoicePayment.addReconcileListItem(reconcile);

    List<InvoiceTerm> invoiceTermList = new ArrayList<InvoiceTerm>();

    invoiceTermList.add(invoiceTerm);

    reconcileService.updateInvoiceTerms(invoiceTermList, invoicePayment, amount, reconcile);

    invoiceTerm = updateInvoiceTermsAmountsSessiontPart(invoiceTerm);
    return invoiceTerm;
  }

  protected boolean getIsSignedNegative(InvoiceTerm invoiceTerm) {
    boolean isSignedNegative = false;
    if (invoiceTerm.getMoveLine() != null) {
      if (invoiceTerm.getMoveLine().getMove().getFunctionalOriginSelect()
          == MoveRepository.FUNCTIONAL_ORIGIN_SALE) {
        isSignedNegative =
            invoiceTerm
                    .getMoveLine()
                    .getDebit()
                    .subtract(invoiceTerm.getMoveLine().getCredit())
                    .signum()
                < 0;
      } else if (invoiceTerm.getMoveLine().getMove().getFunctionalOriginSelect()
          == MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE) {
        isSignedNegative =
            invoiceTerm
                    .getMoveLine()
                    .getCredit()
                    .subtract(invoiceTerm.getMoveLine().getDebit())
                    .signum()
                < 0;
      }
    }
    return isSignedNegative;
  }

  protected InvoiceTerm updateInvoiceTermsAmountsSessiontPart(InvoiceTerm invoiceTerm) {
    boolean isSignedNegative = this.getIsSignedNegative(invoiceTerm);

    if (isSignedNegative) {
      invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining().negate());

    } else {
      invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining());
    }
    this.computeAmountPaid(invoiceTerm);

    return invoiceTerm;
  }

  @Override
  public BigDecimal computeParentTotal(Context context) {
    BigDecimal total = BigDecimal.ZERO;
    if (context.getParent() != null) {
      Invoice invoice = ContextTool.getContextParent(context, Invoice.class, 1);
      if (invoice != null) {
        total = invoice.getInTaxTotal();
      } else {
        MoveLine moveLine = ContextTool.getContextParent(context, MoveLine.class, 1);
        if (moveLine != null) {
          total = moveLine.getDebit().max(moveLine.getCredit());
        }
      }
    }
    return total;
  }
}