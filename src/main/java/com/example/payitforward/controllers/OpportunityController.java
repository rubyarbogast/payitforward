package com.example.payitforward.controllers;

import com.example.payitforward.models.User;
import com.example.payitforward.models.data.OpportunityDao;
import com.example.payitforward.models.data.UserDao;
import com.example.payitforward.models.Opportunity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("opportunity")
public class OpportunityController {

    @Autowired
    private UserDao userDao;

    @Autowired
    OpportunityDao opportunityDao;
//

    @RequestMapping(value = "")
    public String index(Model model) {

        model.addAttribute("opportunities", opportunityDao.findAll());
        model.addAttribute("title", "Opportunities");

        return "opportunity/index";
    }



    @RequestMapping(value = "{opportunityId}",method=RequestMethod.GET)
    public String displayOpportunity(Model model, @PathVariable int opportunityId, HttpSession session) {

        session.setAttribute("claimedError", false);
        Opportunity opportunityToSee = opportunityDao.findOne(opportunityId);

        model.addAttribute("opportunity", opportunityToSee);



        return "opportunity/opportunityPage";
    }


    @RequestMapping(value = "{opportunityId}",method=RequestMethod.POST)
    public String processClaimAndCompletion(Model model, HttpSession session,
                                            @PathVariable int opportunityId){

        User currentUser = (User) session.getAttribute("loggedInUser");

        Opportunity opportunityToEdit = opportunityDao.findOne(opportunityId);

        User creator = opportunityToEdit.getOpportunityCreator();

        List<User> currentClaimedUsers = opportunityToEdit.getClaimingUsers();

        List<User> currentCompletedUsers = opportunityToEdit.getCompletingUsers();

        Boolean userClaimed = false;
        Boolean userCompleted = false;

         for (int i =0; i<currentClaimedUsers.size(); i++){
              if (currentClaimedUsers.get(i).getId() == currentUser.getId()){
                  userClaimed = true;
              }
         }

        for (int i =0; i<currentCompletedUsers.size(); i++){
            if (currentCompletedUsers.get(i).getId() == currentUser.getId()){
                userCompleted = true;
            }
        }

        if (opportunityToEdit.getClaimed() > 0 && currentUser.getId() != creator.getId() && userClaimed==false && userCompleted==false) {
            opportunityToEdit.setClaimed(opportunityToEdit.getClaimed() - 1);

            currentClaimedUsers.add(currentUser);
            opportunityToEdit.setClaimingUsers(currentClaimedUsers);
            opportunityDao.save(opportunityToEdit);
        }

        else if (currentUser.getId() != creator.getId() && userClaimed==true && userCompleted==false) {

            currentCompletedUsers.add(currentUser);
            opportunityToEdit.setCompletingUsers(currentCompletedUsers);
            opportunityDao.save(opportunityToEdit);
        }

        else if (currentUser.getId() == creator.getId() || userCompleted==true) {

            session.setAttribute("claimedError", true);
            return "redirect:/opportunity/{opportunityId}";
        }

        return "redirect:/opportunity/{opportunityId}";
    }

    @RequestMapping(value = "add", method = RequestMethod.GET)
    public String displayAddForm(Model model, HttpSession session) {

        //User user = opportunityDao.findOne(userId);
       // AddOpportunityForm form = new AddOpportunityForm(userDao.findAll(), user);
        if (session.getAttribute("loggedInUser") == null){
            return "redirect:/opportunity";
        }
        model.addAttribute("title", "Add Opportunity");
        model.addAttribute(new Opportunity());
        //model.addAttribute("opportunities",form);


        return "opportunity/add";
    }

//
    @RequestMapping(value = "add", method = RequestMethod.POST)
    public String processAddForm(@ModelAttribute @Valid Opportunity opportunity,
                             Errors errors , Model model, HttpSession session) {

    if (errors.hasErrors()) {
        model.addAttribute("title", "Add Opportunity");
        //  model.addAttribute("user", userDao.findAll());
        return "opportunity/add";
    }

    User currentUser = (User) session.getAttribute("loggedInUser");


    opportunity.setOpportunityCreator(currentUser);
    opportunityDao.save(opportunity);

    return "redirect:/opportunity";
}

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    public String displayRemoveOpportunityForm(Model model) {
        model.addAttribute("opportunities", opportunityDao.findAll());
        model.addAttribute("title", "Remove Opportunity");
        return "opportunity/remove";
    }

    @RequestMapping(value = "remove", method = RequestMethod.POST)
    public String processRemoveOpportunityForm(@RequestParam int[] OpportunityIds) {

        // When removing a cheese from list,
        // find all menus that the cheese is in and
        // remove the cheese in each
        // Them, remove the cheese from the list


        for (int  opportunityId :  OpportunityIds) {
            opportunityDao.delete(opportunityId);
        }

        return "redirect:/opportunity";
    }

    @RequestMapping(value = "edit/{opportunityId}", method=RequestMethod.GET)
    public String displayEditForm(Model model, @PathVariable int opportunityId) {

        Opportunity opportunityToEdit = opportunityDao.findOne(opportunityId);

        model.addAttribute("opportunity", opportunityToEdit);
        return "opportunity/edit";

    }

    @RequestMapping(value = "edit/{opportunityId}", method=RequestMethod.POST)
    public String processEditOpportunityForm(@ModelAttribute @Valid Opportunity opportunity,
                                  @RequestParam String name, String description, String location, int claimed,
                                  Errors errors, Model model,
                                  @PathVariable int opportunityId) {

        if(errors.hasErrors()){
            model.addAttribute("opportunity", opportunity);
            return "opportunity/edit";
        }

        Opportunity opportunityToEdit = opportunityDao.findOne(opportunityId);

        opportunityToEdit.setName(name);
        opportunityToEdit.setDescription(description);
        opportunityToEdit.setLocation(location);
        opportunityToEdit.setClaimed(claimed);

        opportunityDao.save(opportunityToEdit);

        return "redirect:/opportunity";

    }


}