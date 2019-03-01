package springapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import springapp.command.ClientCommand;
import springapp.domain.Client;
import springapp.service.ClientService;

/**
 * This controller handles all client related pages
 *
 * Notice the @PreAuthorize annotations on the methods
 */
@Controller
@RequestMapping("/clients") //notice that this path is set at the class level.
public class ClientController {

    // Inject in a ClientService claass
	@Autowired
	ClientService clientService;

    /**
     * Returns the name of the view template that should be used along witht the model to draw the list of clients
     *
     * Note that no addiontal path is specified, and that this method only handles a GET
     * @param model the model to populate for merging  with the view
     * @return the client list page template
     */
	 @PreAuthorize("hasAuthority('LIST_OWNERS')")
	 @GetMapping
	 public String listClients(Model model) {
        List<Client> clients = clientService.getClients();
		model.addAttribute("clients", clients);
        return "clients/listClients";
    }


    /**
     * Generates the model for rendering the specific client page
     * @param id the id of the client we want to render, if the value is 'new' that is for creating a new client
     * @param model the model to populate for merging with the view
     * @return the client edit page template
     */
	 @PreAuthorize("hasAuthority('GET_OWNER')")
	 @GetMapping("/{id}")
	 public String getClient(@PathVariable("id") String id, Model model) {

	    // we could have used a different path for handling the create page but this approach uses the same
        // template for both creating a new client and editing and existing client
	    if(id.equals("new")) {
	        // create an empty command object to merge with the view template
			model.addAttribute("command", new ClientCommand(null));	
		} else {
	        // since we have a valid id, get the client object from the service
			Client client = clientService.getClient(id);
			// we create a client command that can be used when the browser sends the save object
			model.addAttribute("command", new ClientCommand(client));

			// we get the list of pets, and send those as is since we dont need a command to carry changes to the pets
            // from this page
			model.addAttribute("pets", clientService.getPets(client.getId()) );
		}
		return "clients/editClient";
	}

    /**
     * Saves the updates to a client based on the command that was sent from the client side
     * @param command the command corresponding with how the client object should be updated/created
     * @param model the model to populate, and that will be merged in with the view template to draw the page
     * @return the edit client view template
     */
	 @PreAuthorize("hasAuthority('SAVE_OWNER')")
	 @PostMapping
	 public String saveClient(ClientCommand command, Model model) {

	     //NOTE: if we want to capture errors correctly, we would wrap the following code in a try/catch
         // and the catch would add a nice error message to the mode
         // then the view template would render a nice error message

	     // we pass the command to the service, and it nows how update/create a client
         // the service returns the new client object back to us after the save
	     Client client = clientService.saveClient(command);

	     // we create a new command object from the client object the service returned
	     model.addAttribute("command", new ClientCommand(client));

	     // we get the pets for te client
	     model.addAttribute("pets", clientService.getPets(client.getId()) );

	     // we add in a "saved" attribute so we can print a nice message indicating a save was successfull
		 model.addAttribute("saved", true);


	     return "clients/editClient";
		  
     }

    /**
     * Deletes a client and redirects to list clients page
     * @param id the id of the client to delete
     * @param redirectAttributes we use this instead of a Model object, because we want to pass
     *                           some attributes to the list page
     * @return redirect path to the list clients page
     */
     @PreAuthorize("hasAuthority('DELETE_OWNER')")
	 @GetMapping("/{id}/delete")
	 public String deleteClient(@PathVariable("id") String id, RedirectAttributes redirectAttributes) {
         // NOTE to handle exceptions, we would wrap the following code in a try/catch
         // and in the catch forward to a different page

         // send the id passed in to the client service
         clientService.deleteClient(id);

         // add an attribute to the list page, so a nice message can be shown
         redirectAttributes.addFlashAttribute("deleted", true);

         // redirect to list clients path/page
         return "redirect:/clients";
    }

}