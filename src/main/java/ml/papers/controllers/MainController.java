package ml.papers.controllers;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import ml.papers.services.AuthorService;
import ml.papers.services.OwnershipService;
import ml.papers.services.PaperService;
import ml.papers.entities.tables.pojos.Paper;

import java.util.Collection;
import java.util.Set;

@Controller("/")
public class MainController {

    private final PaperService paperService;
    private final AuthorService authorService;
    private final OwnershipService ownershipService;

    public MainController(PaperService paperService, AuthorService authorService, OwnershipService ownershipService) {
        this.paperService = paperService;
        this.authorService = authorService;
        this.ownershipService = ownershipService;
    }

    @Get
    public Paper getPaper() {
        return paperService.getRandomPaper();
    }

    @Get("/fetch-papers-ids")
    public Collection<Integer> getPaperIds() {
        return paperService.getPapersIds();
    }

    @Get("/most-hardworking-author")
    public String getMostHardworkingAuthor() {
        return authorService.getMostHardworkingAuthor();
    }

    @Get("/fetchAuthors")
    public Set<String> fetchAuthors() {
        return paperService.getAuthors();
    }

    @Post(value = "/insert-authors", consumes = MediaType.APPLICATION_JSON)
    public void insertAuthors(@Body Collection<String> authorNames) {
        authorService.insertAuthors(authorNames);
    }

    @Post(value = "/insert-papers-ownership", consumes = MediaType.APPLICATION_JSON)
    public void insertPapersOwnership(@Body Collection<Integer> paperIds) {
        ownershipService.insertPapersOwnership(paperIds);
    }
}
