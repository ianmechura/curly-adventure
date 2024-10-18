package integrations.turnitin.com.membersearcher.service;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import integrations.turnitin.com.membersearcher.client.MembershipBackendClient;
import integrations.turnitin.com.membersearcher.model.Membership;
import integrations.turnitin.com.membersearcher.model.MembershipList;

import integrations.turnitin.com.membersearcher.model.User;
import integrations.turnitin.com.membersearcher.model.UserList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MembershipService {
    @Autowired
    private MembershipBackendClient membershipBackendClient;

    /**
     * Method to fetch all memberships with their associated user details included.
     * This method calls out to the php-backend service and fetches all memberships,
     * and all users. The memberships are enriched with user details from the corresponding user
     * that was returned in the getUsers response.
     *
     * @return A CompletableFuture containing a fully populated MembershipList object.
     */
    public CompletableFuture<MembershipList> fetchAllMembershipsWithUsers() {

        CompletableFuture<Map<String, User>> fetchAndMapUsersFuture =
                membershipBackendClient.fetchUsers()
                        .thenApply(users -> users.getUsers().stream().collect(Collectors.toMap(User::getId, u -> u)));

        return membershipBackendClient.fetchMemberships().thenCombine(fetchAndMapUsersFuture, (memberList, userMap) -> {
            memberList.getMemberships().stream().map(membership ->
                    membership.setUser(userMap.get(membership.getUserId()))).toArray();
            return memberList;
        });
    }
}
