package PoolC.Comect.relation;

import PoolC.Comect.data.domain.Data;
import PoolC.Comect.data.dto.FolderDeleteRequestDto;
import PoolC.Comect.data.repository.DataRepository;
import PoolC.Comect.relation.domain.Relation;
import PoolC.Comect.relation.domain.RelationType;
import PoolC.Comect.relation.dto.*;
import PoolC.Comect.relation.repository.RelationRepository;
import PoolC.Comect.user.domain.User;
import PoolC.Comect.user.repository.UserRepository;
import PoolC.Comect.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment =SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class relationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private DataRepository dataRepository;
    @Autowired
    private RelationRepository relationRepository;
    @Autowired
    private UserRepository userRepository;

    private ObjectMapper mapper = new ObjectMapper();


    @Before
    public void before(){
        dataRepository.deleteAll();
        relationRepository.deleteAll();
        userRepository.deleteAll();

        Data data1 = new Data();
        User user1 = new User("user1", "user1Email@naver.com", data1.getId(), "user1Picture", "1234");
        dataRepository.save(data1);
        userRepository.save(user1);
        Data data2 = new Data();
        User user2 = new User("user2", "user2Email@naver.com", data2.getId(), "user2Picture", "5678");
        dataRepository.save(data2);
        userRepository.save(user2);
        Data data3 = new Data();
        User user3 = new User("user3", "user3Email@naver.com", data1.getId(), "user3Picture", "1234");
        dataRepository.save(data3);
        userRepository.save(user3);
        Data data4 = new Data();
        User user4 = new User("user4", "user4Email@naver.com", data1.getId(), "user4Picture", "12345");
        dataRepository.save(data4);
        userRepository.save(user4);

        User temp1 = userRepository.findByNickname("user1").get();
        User temp2 = userRepository.findByNickname("user2").get();

        Relation d=new Relation(temp1.getId(),temp2.getId());
        User temp4 = userRepository.findByNickname("user4").get();
        Relation d1=new Relation(temp1.getId(),temp4.getId());
        d1.setRelationType(RelationType.BOTH);
        relationRepository.save(d);
        relationRepository.save(d1);
        user1.getRelations().add(d.getId());
        user1.getRelations().add(d1.getId());
        user2.getRelations().add(d.getId());
        user4.getRelations().add(d1.getId());
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user4);
    }

    @Test
    @DisplayName("테스트 01 : 친구목록, 신청목록 가져오기")
    public void friendList1() throws URISyntaxException, JsonProcessingException {
        //given
        final String baseUrl = "http://localhost:" + port + "/friend?email=user1Email@naver.com";
        URI uri = new URI(baseUrl);
        //when
        ResponseEntity<String> result = this.restTemplate.getForEntity(uri,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode root = mapper.readTree(result.getBody());
        assertThat(root.path("numberOfRequest").asInt()).isEqualTo(0);
        assertThat(root.path("numberOfFriend").asInt()).isEqualTo(1);
        assertThat(root.path("friends").get(0).path("email").asText()).isEqualTo("user4Email@naver.com");
    }

    @Test
    @DisplayName("테스트 01-1 : 친구목록, 신청목록 가져오기, 데이터가 없음")
    public void friendList2() throws URISyntaxException {
        //given
        final String baseUrl = "http://localhost:" + port + "/friend?email=user3Email@naver.com";
        URI uri = new URI(baseUrl);
        //when
        ResponseEntity<String> result = this.restTemplate.getForEntity(uri,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("테스트 02 : 친구목록, 신청목록 가져오기, 없는 이메일")
    public void friendList3() throws URISyntaxException {
        //given
        final String baseUrl = "http://localhost:" + port + "/friend?email=user333Email@naver.com";
        URI uri = new URI(baseUrl);
        //when
        ResponseEntity<String> result = this.restTemplate.getForEntity(uri,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("테스트 03 : 친구 신청")
    public void friendRequest1() throws URISyntaxException {
        //given
        final String baseUrl = "http://localhost:" + port + "/friend";
        URI uri = new URI(baseUrl);
        CreateRelationRequestDto createRelationRequestDto = new CreateRelationRequestDto();
        createRelationRequestDto.setEmail("user1Email@naver.com");
        createRelationRequestDto.setFriendNickname("user3");
        //when
        ResponseEntity<String> result = this.restTemplate.postForEntity(uri,createRelationRequestDto,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        User reciever = userRepository.findByEmail("user3Email@naver.com").get();
        User sender = userRepository.findByEmail("user1Email@naver.com").get();
        Relation relation = relationRepository.findById(reciever.getRelations().get(0)).get();
        assertThat(relation.getRelationType()).isEqualTo(RelationType.REQUEST);
        assertThat(relation.getReceiverId()).isEqualTo(reciever.getId());
        assertThat(relation.getSenderId()).isEqualTo(sender.getId());
        assertThat(reciever.getRelations().get(0)).isEqualTo(relation.getId());
        assertThat(sender.getRelations().get(2)).isEqualTo(relation.getId());
    }

    @Test
    @DisplayName("테스트 04 : 친구 신청, 이미 있는 신청")
    public void friendRequest2() throws URISyntaxException {
        //given
        final String baseUrl = "http://localhost:" + port + "/friend";
        URI uri = new URI(baseUrl);
        CreateRelationRequestDto createRelationRequestDto = new CreateRelationRequestDto();
        createRelationRequestDto.setEmail("user1Email@naver.com");
        createRelationRequestDto.setFriendNickname("user2");
        //when
        ResponseEntity<String> result = this.restTemplate.postForEntity(uri,createRelationRequestDto,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("테스트 05 : 친구 수락")
    public void friendAccept1() throws URISyntaxException{
        //given
        final String baseUrl = "http://localhost:" + port + "/friend/request";
        URI uri = new URI(baseUrl);
        AcceptRelationRequestDto acceptRelationRequestDto = new AcceptRelationRequestDto();
        acceptRelationRequestDto.setEmail("user2Email@naver.com");
        acceptRelationRequestDto.setFriendNickname("user1");
        //when
        ResponseEntity<String> result = this.restTemplate.postForEntity(uri,acceptRelationRequestDto,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        User user2 = userRepository.findByNickname("user2").get();
        assertThat(relationRepository.findById(user2.getRelations().get(0)).get().getRelationType()).isEqualTo(RelationType.BOTH);
    }

    @Test
    @DisplayName("테스트 06 : 친구 수락, 내가 요청한 친구")
    public void friendAccept2() throws URISyntaxException{
        //given
        final String baseUrl = "http://localhost:" + port + "/friend/request";
        URI uri = new URI(baseUrl);
        AcceptRelationRequestDto acceptRelationRequestDto = new AcceptRelationRequestDto();
        acceptRelationRequestDto.setEmail("user1Email@naver.com");
        acceptRelationRequestDto.setFriendNickname("user2");
        //when
        ResponseEntity<String> result = this.restTemplate.postForEntity(uri,acceptRelationRequestDto,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("테스트 07 : 친구 수락, 이미 수락한 친구")
    public void friendAccept3() throws URISyntaxException{
        //given
        final String baseUrl = "http://localhost:" + port + "/friend/request";
        URI uri = new URI(baseUrl);
        AcceptRelationRequestDto acceptRelationRequestDto = new AcceptRelationRequestDto();
        acceptRelationRequestDto.setEmail("user4Email@naver.com");
        acceptRelationRequestDto.setFriendNickname("user1");
        //when
        ResponseEntity<String> result = this.restTemplate.postForEntity(uri,acceptRelationRequestDto,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("테스트 08 : 친구 수락, 없는 사용자")
    public void friendAccept4() throws URISyntaxException{
        //given
        final String baseUrl = "http://localhost:" + port + "/friend/request";
        URI uri = new URI(baseUrl);
        AcceptRelationRequestDto acceptRelationRequestDto = new AcceptRelationRequestDto();
        acceptRelationRequestDto.setEmail("user10Email@naver.com");
        acceptRelationRequestDto.setFriendNickname("user4");
        //when
        ResponseEntity<String> result = this.restTemplate.postForEntity(uri,acceptRelationRequestDto,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("테스트 09 : 친구 수락, 없는 요청")
    public void friendAccept5() throws URISyntaxException{
        //given
        final String baseUrl = "http://localhost:" + port + "/friend/request";
        URI uri = new URI(baseUrl);
        AcceptRelationRequestDto acceptRelationRequestDto = new AcceptRelationRequestDto();
        acceptRelationRequestDto.setEmail("user1");
        acceptRelationRequestDto.setFriendNickname("user3Email@naver.com");
        //when
        ResponseEntity<String> result = this.restTemplate.postForEntity(uri,acceptRelationRequestDto,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("테스트 10 : 친구 거절")
    public void friendReject1() throws URISyntaxException{
        //given
        final String baseUrl = "http://localhost:" + port + "/friend/request";
        URI uri = new URI(baseUrl);
        RejectRelationRequestDto rejectRelationRequestDto = new RejectRelationRequestDto();
        rejectRelationRequestDto.setEmail("user2Email@naver.com");
        rejectRelationRequestDto.setFriendNickname("user1");
        //when
        HttpHeaders headers = new HttpHeaders();
        headers.set("JSON","true");
        HttpEntity<RejectRelationRequestDto> request = new HttpEntity<>(rejectRelationRequestDto,headers);
        ResponseEntity<String> result = this.restTemplate.exchange(uri, HttpMethod.DELETE,request,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

        User user2 = userRepository.findByNickname("user2").get();
        assertThat(relationRepository.findById(user2.getRelations().get(0)).get().getRelationType()).isEqualTo(RelationType.REJECTED);
    }

    @Test
    @DisplayName("테스트 11 : 친구 거절, 내가 신청한 요청")
    public void friendReject2() throws URISyntaxException{
        //given
        final String baseUrl = "http://localhost:" + port + "/friend/request";
        URI uri = new URI(baseUrl);
        RejectRelationRequestDto rejectRelationRequestDto = new RejectRelationRequestDto();
        rejectRelationRequestDto.setEmail("user1Email@naver.com");
        rejectRelationRequestDto.setFriendNickname("user2");
        //when
        HttpHeaders headers = new HttpHeaders();
        headers.set("JSON","true");
        HttpEntity<RejectRelationRequestDto> request = new HttpEntity<>(rejectRelationRequestDto,headers);
        ResponseEntity<String> result = this.restTemplate.exchange(uri, HttpMethod.DELETE,request,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("테스트 12 : 친구 거절, 없는 사용자")
    public void friendReject3() throws URISyntaxException{
        //given
        final String baseUrl = "http://localhost:" + port + "/friend/request";
        URI uri = new URI(baseUrl);
        RejectRelationRequestDto rejectRelationRequestDto = new RejectRelationRequestDto();
        rejectRelationRequestDto.setEmail("user5Email@naver.com");
        rejectRelationRequestDto.setFriendNickname("user2");
        //when
        HttpHeaders headers = new HttpHeaders();
        headers.set("JSON","true");
        HttpEntity<RejectRelationRequestDto> request = new HttpEntity<>(rejectRelationRequestDto,headers);
        ResponseEntity<String> result = this.restTemplate.exchange(uri, HttpMethod.DELETE,request,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test
    @DisplayName("테스트 13 : 친구 삭제")
    public void friendDelete1() throws URISyntaxException{
        //given
        final String baseUrl = "http://localhost:" + port + "/friend";
        URI uri = new URI(baseUrl);
        DeleteFriendRequestDto deleteFriendRequestDto = new DeleteFriendRequestDto();
        deleteFriendRequestDto.setEmail("user1Email@naver.com");
        deleteFriendRequestDto.setFriendNickname("user4");
        //when
        HttpHeaders headers = new HttpHeaders();
        headers.set("JSON","true");
        HttpEntity<DeleteFriendRequestDto> request = new HttpEntity<>(deleteFriendRequestDto,headers);
        ResponseEntity<String> result = this.restTemplate.exchange(uri, HttpMethod.DELETE,request,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        User user4 = userRepository.findByNickname("user4").get();
        assertThat(relationRepository.findById(user4.getRelations().get(0)).get().getRelationType()).isEqualTo(RelationType.DELETED);
    }
    @Test
    @DisplayName("테스트 14 : 친구 삭제, 없는 요청")
    public void friendDelete2() throws URISyntaxException{
        //given
        final String baseUrl = "http://localhost:" + port + "/friend";
        URI uri = new URI(baseUrl);
        DeleteFriendRequestDto deleteFriendRequestDto = new DeleteFriendRequestDto();
        deleteFriendRequestDto.setEmail("user1Email@naver.com");
        deleteFriendRequestDto.setFriendNickname("user3");
        //when
        HttpHeaders headers = new HttpHeaders();
        headers.set("JSON","true");
        HttpEntity<DeleteFriendRequestDto> request = new HttpEntity<>(deleteFriendRequestDto,headers);
        ResponseEntity<String> result = this.restTemplate.exchange(uri, HttpMethod.DELETE,request,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test
    @DisplayName("테스트 15 : 친구 삭제, 이미 삭제된 요청")
    public void friendDelete3() throws URISyntaxException{
        //given
        final String baseUrl = "http://localhost:" + port + "/friend";
        URI uri = new URI(baseUrl);
        DeleteFriendRequestDto deleteFriendRequestDto = new DeleteFriendRequestDto();
        deleteFriendRequestDto.setEmail("user1Email@naver.com");
        deleteFriendRequestDto.setFriendNickname("user4");
        //when
        HttpHeaders headers = new HttpHeaders();
        headers.set("JSON","true");
        HttpEntity<DeleteFriendRequestDto> request = new HttpEntity<>(deleteFriendRequestDto,headers);
        ResponseEntity<String> result = this.restTemplate.exchange(uri, HttpMethod.DELETE,request,String.class);
        //then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        User user4 = userRepository.findByNickname("user4").get();
        assertThat(relationRepository.findById(user4.getRelations().get(0)).get().getRelationType()).isEqualTo(RelationType.DELETED);

        ResponseEntity<String> result1 = this.restTemplate.exchange(uri, HttpMethod.DELETE,request,String.class);
        assertThat(result1.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
