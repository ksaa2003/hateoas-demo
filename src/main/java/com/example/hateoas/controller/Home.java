package com.example.hateoas.controller;

import com.example.hateoas.exception.FrozenException;
import com.example.hateoas.model.*;
import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class Home {
    private static Account account1 = new Account(1, 50.0, Status.Active);
    private static Account account2 = new Account(2, 0.0, Status.Active);
    private static List<Account> accounts = new ArrayList<>();

    public Home() {
        account1.add(linkTo(methodOn(Home.class).getAccountById(account1.getId())).withRel(Relationships.SELF));
        account1.add(linkTo(methodOn(Home.class).deposit(account1.getId(), null)).withRel(Relationships.DEPOSIT));
        account1.add(linkTo(methodOn(Home.class).withdraw(account1.getId(), null)).withRel(Relationships.WITHDRAW));
        account1.add(linkTo(methodOn(Home.class).freeze(account1.getId())).withRel(Relationships.FREEZE));

        account2.add(linkTo(methodOn(Home.class).getAccountById(account2.getId())).withRel(Relationships.SELF));
        account2.add(linkTo(methodOn(Home.class).deposit(account2.getId(), null)).withRel(Relationships.DEPOSIT));
        account2.add(linkTo(methodOn(Home.class).freeze(account2.getId())).withRel(Relationships.FREEZE));

        accounts.add(account1);
        accounts.add(account2);
    }

    @GetMapping(value = "/api", produces = "application/json")
    public ApiHome getHome() {
        var apiHome = new ApiHome();
        apiHome.add(linkTo(methodOn(Home.class).getHome()).withRel(Relationships.SELF));
        apiHome.add(linkTo(methodOn(Home.class).getAccounts()).withRel(Relationships.ACCOUNTS));

        return apiHome;
    }

    @GetMapping(value = "api/accounts", produces = "application/json")
    public CollectionModel<Account> getAccounts() {
        var accountsLink = linkTo(methodOn(Home.class)
                .getAccounts()).withRel(Relationships.SELF);
        var createLink = linkTo(methodOn(Home.class).createAccount(null)).withRel(Relationships.CREATE_ACCOUNT);

        return CollectionModel.of(accounts, accountsLink, createLink);
    }

    @PostMapping(value = "api/accounts", produces = "application/json")
    public Account createAccount(@RequestBody BalanceAdjustmentRequest createRequest) {
        var id = accounts.get(accounts.size() - 1).getId() + 1;
        var newAccount = new Account(id, createRequest.getBalance(), Status.Active);
        newAccount.add(linkTo(methodOn(Home.class).getAccountById(id)).withRel(Relationships.SELF));
        newAccount.add(linkTo(methodOn(Home.class).freeze(id)).withRel(Relationships.FREEZE));
        newAccount.add(linkTo(methodOn(Home.class).deposit(id, null)).withRel(Relationships.DEPOSIT));
        newAccount.add(linkTo(methodOn(Home.class).getAccounts()).withRel(Relationships.ACCOUNTS));

        if (newAccount.getBalance() > 0) {
            newAccount.add(linkTo(methodOn(Home.class).withdraw(id, null)).withRel(Relationships.WITHDRAW));
        }

        accounts.add(newAccount);

        return newAccount;
    }

    @GetMapping(value = "api/accounts/{id}", produces = "application/json")
    public Account getAccountById(@PathVariable("id") int id) {
        var account = accounts.stream()
                .filter(a -> a.getId() == id)
                .findFirst()
                .get();

        if (account.getLink(Relationships.ACCOUNTS) == null) {
            account.add(linkTo(methodOn(Home.class).getAccounts()).withRel(Relationships.ACCOUNTS));
        }

        return account;
    }

    @PatchMapping(value = "api/accounts/{id}/deposit", produces = "application/json")
    public Account deposit(@PathVariable("id") int id, @RequestBody BalanceAdjustmentRequest depositRequest) {
        var accountToDepositTo = accounts.stream()
                .filter(account -> account.getId() == id)
                .findFirst()
                .get();

        if (accountToDepositTo.getStatus() == Status.Frozen) {
            throw new FrozenException(id, "The account is frozen and cannot do deposits");
        }

        accountToDepositTo.setBalance(accountToDepositTo.getBalance() + depositRequest.getBalance());
        if ((accountToDepositTo.getBalance() + depositRequest.getBalance()) > 0) {
            accountToDepositTo.add(linkTo(methodOn(Home.class).withdraw(id, null)).withRel(Relationships.WITHDRAW));
        }

        if (accountToDepositTo.getLink(Relationships.FREEZE) == null) {
            accountToDepositTo.add(linkTo(methodOn(Home.class).freeze(accountToDepositTo.getId())).withRel(Relationships.FREEZE));
        }

        return accountToDepositTo;
    }

    @PatchMapping(value = "api/accounts/{id}/withdraw", produces = "application/json")
    public Account withdraw(@PathVariable("id") int id, @RequestBody BalanceAdjustmentRequest withdrawRequest) {
        var accountToWithdrawFrom = accounts.stream()
                .filter(account -> account.getId() == id)
                .findFirst()
                .get();

        if (accountToWithdrawFrom.getStatus() == Status.Frozen) {
            throw new FrozenException(id, "The account is frozen and cannot do withdraws");
        }

        if ((accountToWithdrawFrom.getBalance() - withdrawRequest.getBalance()) <= 0) {
            accountToWithdrawFrom.removeLinks();
            accountToWithdrawFrom.add(linkTo(methodOn(Home.class).getAccountById(account1.getId())).withRel(Relationships.SELF));
            accountToWithdrawFrom.add(linkTo(methodOn(Home.class).deposit(account1.getId(), null)).withRel(Relationships.DEPOSIT));
        }

        if ((accountToWithdrawFrom.getBalance() - withdrawRequest.getBalance()) >= 0) {
            accountToWithdrawFrom.setBalance(accountToWithdrawFrom.getBalance() - withdrawRequest.getBalance());
        }

        accountToWithdrawFrom.add(linkTo(methodOn(Home.class).freeze(accountToWithdrawFrom.getId())).withRel(Relationships.FREEZE));

        return accountToWithdrawFrom;
    }

    @PatchMapping(value = "api/accounts/{id}/freeze", produces = "application/json")
    public Account freeze(@PathVariable("id") int id) {
        var accountToFreeze = accounts.stream()
                .filter(account -> account.getId() == id)
                .findFirst()
                .get();
        accountToFreeze.setStatus(Status.Frozen);
        accountToFreeze.removeLinks();
        accountToFreeze.add(linkTo(methodOn(Home.class).getAccountById(accountToFreeze.getId())).withRel(Relationships.SELF));
        accountToFreeze.add(linkTo(methodOn(Home.class).unfreeze(accountToFreeze.getId())).withRel(Relationships.UNFREEZE));
        return accountToFreeze;
    }

    @PatchMapping(value = "api/accounts/{id}/unfreeze", produces = "application/json")
    public Account unfreeze(@PathVariable("id") int id) {
        var accountToUnfreeze = accounts.stream()
                .filter(account -> account.getId() == id)
                .findFirst()
                .get();
        accountToUnfreeze.setStatus(Status.Active);
        accountToUnfreeze.removeLinks();
        accountToUnfreeze.add(linkTo(methodOn(Home.class).getAccountById(accountToUnfreeze.getId())).withRel(Relationships.SELF));
        accountToUnfreeze.add(linkTo(methodOn(Home.class).freeze(accountToUnfreeze.getId())).withRel(Relationships.FREEZE));
        accountToUnfreeze.add(linkTo(methodOn(Home.class).deposit(accountToUnfreeze.getId(), null)).withRel(Relationships.DEPOSIT));
        if (accountToUnfreeze.getBalance() > 0) {
            accountToUnfreeze.add(linkTo(methodOn(Home.class).withdraw(id, null)).withRel(Relationships.WITHDRAW));
        }

        return accountToUnfreeze;
    }
}
