package edu.rit.se.satd;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


/**
 * A class which stores and categorizes different SATD instances
 * and maintains logic to merge appropriate entries
 */
@RequiredArgsConstructor
public class SATDDifference {

    // Required fields for maintaining an SATD Difference object
    @Getter
    @NonNull
    private String projectName;
    @Getter
    @NonNull
    private String projectURI;
    @Getter
    @NonNull
    private RevCommit oldCommit;
    @Getter
    @NonNull
    private RevCommit newCommit;

    // The lists of the different types of SATD that can be found in a project
    @Getter
    private List<SATDInstance> satdInstances = new ArrayList<>();

    public void addSATDInstances(List<SATDInstance> satd) {
        this.satdInstances.addAll(satd);
    }

}
