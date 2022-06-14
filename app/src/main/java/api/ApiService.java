package api;

public class ApiService {

        public enum PLAYER_ID{

                HR75("pl:id:hr75"),
                PHOENIX("pl:id:phoenix");

                String id;
                PLAYER_ID(String playerId) {
                        id = playerId;
                }
                public String getId(){
                        return id;
                }
        }

        public PLAYER_ID getPlayerById(String id){
                switch (id) {
                        case "pl:id:hr75": return PLAYER_ID.HR75;
                        case "pl:id:phoenix": return PLAYER_ID.PHOENIX;
                        default: return null;
                }
        }

        //TODO: Make this method learn how to return a PlayerModel, only in pc version
        public void getPlayerModel(PLAYER_ID id) {}

}
