package controllers;

import models.Computer;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import repository.ChallengeRepository;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Manage a database of computers
 */
public class HomeController extends Controller {

    private final FormFactory formFactory;
    private final HttpExecutionContext httpExecutionContext;
    private final ChallengeRepository challengeRepository;

    @Inject
    public HomeController(
            FormFactory formFactory,
            HttpExecutionContext httpExecutionContext,
            ChallengeRepository challengeRepository
    ) {
        this.formFactory = formFactory;
        this.httpExecutionContext = httpExecutionContext;
        this.challengeRepository = challengeRepository;
    }

    /**
     * This result directly redirect to application home.
     */
    private Result GO_HOME = Results.redirect(
            routes.HomeController.list()
    );

    /**
     * Handle default path requests, redirect to computers list
     */
    public Result index() {
        return GO_HOME;
    }

    /**
     * Display the paginated list of computers.
     *
     * @param page   Current page number (starts from 0)
     * @param sortBy Column to be sorted
     * @param order  Sort order (either asc or desc)
     * @param filter Filter applied on computer names
     */
    public CompletionStage<Result> list() {

        return challengeRepository.all().thenApplyAsync(list -> {
            // This is the HTTP rendering thread context
            return ok(views.html.list.render(list));
        }, httpExecutionContext.current());
    }

    /**
     * Display the 'edit form' of a existing Computer.
     *
     * @param id Id of the computer to edit
     */
    public CompletionStage<Result> edit(Long id) {

        // Run a db operation in another thread (using DatabaseExecutionContext)
        CompletionStage<Map<String, String>> companiesFuture = companyRepository.options();

        // Run the lookup also in another thread, then combine the results:
        return challengeRepository.lookup(id).thenCombineAsync(companiesFuture, (computerOptional, companies) -> {
            // This is the HTTP rendering thread context
            Computer c = computerOptional.get();
            Form<Computer> computerForm = formFactory.form(Computer.class).fill(c);
            return ok(views.html.editForm.render(id, computerForm, companies));
        }, httpExecutionContext.current());
    }

    /**
     * Handle the 'edit form' tournamentEntry
     *
     * @param id Id of the computer to edit
     */
    public CompletionStage<Result> update(Long id) throws PersistenceException {
        Form<Computer> computerForm = formFactory.form(Computer.class).bindFromRequest();
        if (computerForm.hasErrors()) {
            // Run companies db operation and then render the failure case
            return companyRepository.options().thenApplyAsync(companies -> {
                // This is the HTTP rendering thread context
                return badRequest(views.html.editForm.render(id, computerForm, companies));
            }, httpExecutionContext.current());
        } else {
            Computer newComputerData = computerForm.get();
            // Run update operation and then flash and then redirect
            return challengeRepository.update(id, newComputerData).thenApplyAsync(data -> {
                // This is the HTTP rendering thread context
                flash("success", "Computer " + newComputerData.name + " has been updated");
                return GO_HOME;
            }, httpExecutionContext.current());
        }
    }

    /**
     * Display the 'new computer form'.
     */
    public CompletionStage<Result> create() {
        Form<Computer> computerForm = formFactory.form(Computer.class);
        // Run companies db operation and then render the form
        return companyRepository.options().thenApplyAsync((Map<String, String> companies) -> {
            // This is the HTTP rendering thread context
            return ok(views.html.createForm.render(computerForm, companies));
        }, httpExecutionContext.current());
    }

    /**
     * Handle the 'new computer form' tournamentEntry
     */
    public CompletionStage<Result> save() {
        Form<Computer> computerForm = formFactory.form(Computer.class).bindFromRequest();
        if (computerForm.hasErrors()) {
            // Run companies db operation and then render the form
            return companyRepository.options().thenApplyAsync(companies -> {
                // This is the HTTP rendering thread context
                return badRequest(views.html.createForm.render(computerForm, companies));
            }, httpExecutionContext.current());
        }

        Computer computer = computerForm.get();
        // Run insert db operation, then redirect
        return challengeRepository.insert(computer).thenApplyAsync(data -> {
            // This is the HTTP rendering thread context
            flash("success", "Computer " + computer.name + " has been created");
            return GO_HOME;
        }, httpExecutionContext.current());
    }

    /**
     * Handle computer deletion
     */
    public CompletionStage<Result> delete(Long id) {
        // Run delete db operation, then redirect
        return challengeRepository.delete(id).thenApplyAsync(v -> {
            // This is the HTTP rendering thread context
            flash("success", "Computer has been deleted");
            return GO_HOME;
        }, httpExecutionContext.current());
    }

}
            
