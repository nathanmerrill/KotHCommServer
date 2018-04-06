package controllers;

import com.google.common.primitives.Longs;
import models.Challenge;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import repository.ChallengeRepository;
import repository.UserRepository;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.util.concurrent.CompletionStage;

/**
 * Manage a database of computers
 */
public class UserController extends Controller {

    private final FormFactory formFactory;
    private final HttpExecutionContext httpExecutionContext;
    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    @Inject
    public UserController(
            FormFactory formFactory,
            HttpExecutionContext httpExecutionContext,
            ChallengeRepository challengeRepository,
            UserRepository userRepository
    ) {
        this.formFactory = formFactory;
        this.httpExecutionContext = httpExecutionContext;
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Handle default path requests, redirect to computers list
     */
    public Result me() {
        Long userId = Longs.tryParse(session().get("user_id"));
        return Results.redirect(
                userId != null ? routes.ChallengeController.view(userId) : routes.ChallengeController.list()
        );
    }

    /**
     * Display the paginated list of computers.
     *
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

        // Run the lookup also in another thread, then combine the results:
        return challengeRepository.view(id).thenApplyAsync((optionalChallenge) -> {
            // This is the HTTP rendering thread context
            if (!optionalChallenge.isPresent()){
                return Results.notFound();
            }
            Challenge challenge = optionalChallenge.get();
            //TODO: Authenticate user


            Form<Challenge> challengeForm = formFactory.form(Challenge.class).fill(challenge);
            return ok(views.html.editForm.render(id, challengeForm));
        }, httpExecutionContext.current());
    }

    /**
     * Display the 'edit form' of a existing Computer.
     *
     * @param id Id of the computer to edit
     */
    public CompletionStage<Result> view(Long id) {

//        // Run a db operation in another thread (using DatabaseExecutionContext)
//        CompletionStage<Map<String, String>> companiesFuture = companyRepository.options();
//
//        // Run the lookup also in another thread, then combine the results:
//        return challengeRepository.lookup(id).thenCombineAsync(companiesFuture, (computerOptional, companies) -> {
//            // This is the HTTP rendering thread context
//            Computer c = computerOptional.get();
//            Form<Computer> computerForm = formFactory.form(Computer.class).fill(c);
//            return ok(views.html.editForm.render(id, computerForm, companies));
//        }, httpExecutionContext.current());
        return null;
    }

    /**
     * Handle the 'edit form' tournamentEntry
     *
     * @param id Id of the computer to edit
     */
    public CompletionStage<Result> update(Long id) throws PersistenceException {
//        Form<Computer> computerForm = formFactory.form(Computer.class).bindFromRequest();
//        if (computerForm.hasErrors()) {
//            // Run companies db operation and then render the failure case
//            return companyRepository.options().thenApplyAsync(companies -> {
//                // This is the HTTP rendering thread context
//                return badRequest(views.html.editForm.render(id, computerForm, companies));
//            }, httpExecutionContext.current());
//        } else {
//            Computer newComputerData = computerForm.get();
//            // Run update operation and then flash and then redirect
//            return challengeRepository.update(id, newComputerData).thenApplyAsync(data -> {
//                // This is the HTTP rendering thread context
//                flash("success", "Computer " + newComputerData.name + " has been updated");
//                return GO_HOME;
//            }, httpExecutionContext.current());
//        }
        return null;
    }

    /**
     * Display the 'new computer form'.
     */
    public CompletionStage<Result> create() {
//        Form<Computer> computerForm = formFactory.form(Computer.class);
//        // Run companies db operation and then render the form
//        return companyRepository.options().thenApplyAsync((Map<String, String> companies) -> {
//            // This is the HTTP rendering thread context
//            return ok(views.html.createForm.render(computerForm, companies));
//        }, httpExecutionContext.current());
        return null;
    }

    /**
     * Handle the 'new computer form' tournamentEntry
     */
    public CompletionStage<Result> save() {
//        Form<Computer> computerForm = formFactory.form(Computer.class).bindFromRequest();
//        if (computerForm.hasErrors()) {
//            // Run companies db operation and then render the form
//            return companyRepository.options().thenApplyAsync(companies -> {
//                // This is the HTTP rendering thread context
//                return badRequest(views.html.createForm.render(computerForm, companies));
//            }, httpExecutionContext.current());
//        }
//
//        Computer computer = computerForm.get();
//        // Run insert db operation, then redirect
//        return challengeRepository.insert(computer).thenApplyAsync(data -> {
//            // This is the HTTP rendering thread context
//            flash("success", "Computer " + computer.name + " has been created");
//            return GO_HOME;
//        }, httpExecutionContext.current());
        return null;
    }

}
            
