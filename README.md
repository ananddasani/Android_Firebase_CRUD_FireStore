# Android_Firebase_CRUD_FireStore
Create Entry, Left Swipe to Update the Entry &amp; Right Swip to Delete Entry in RecyclerView

This topic is a part of [My Complete Andorid Course](https://github.com/ananddasani/Android_Apps)

# Code

#### MainActivity.java
```
EditText name, message;
Button insertButton, showAllMessageButton;

FirebaseFirestore db;

//receiving data to be updated
String recId, recName, recMessage;

        //insert the data into the firestore db
        db = FirebaseFirestore.getInstance();

        name = findViewById(R.id.editTextName);
        message = findViewById(R.id.editTextMessage);
        insertButton = findViewById(R.id.insertButton);
        showAllMessageButton = findViewById(R.id.showAllButton);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            insertButton.setText("Update");

            recId = bundle.getString("id");
            recName = bundle.getString("name");
            recMessage = bundle.getString("message");

            //set the data
            name.setText(recName);
            message.setText(recMessage);

        } else {
            insertButton.setText("Insert");
        }

        insertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = name.getText().toString();
                String userMessage = message.getText().toString();

                if (userMessage.equals("")) {
                    message.setError("Can't be empty");
                    return;
                }

                if (userName.equals("")) {
                    name.setError("Can't be empty");
                    return;
                }

                //check if user want to update the data or insert the data
                Bundle bundle1 = getIntent().getExtras();
                if (bundle1 != null) {
                    //user want to update the data
                    String id = recId;
                    updateTheData(id, userName, userMessage);

                } else {
                    //user want to insert the data

                    //create the random id
                    String id = UUID.randomUUID().toString();

                    saveToFireStore(id, userName, userMessage);

                    //clear the fields
                    name.setText("");
                    message.setText("");
                }
            }
        });

        showAllMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Showing all the messages", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, ShowMessage.class));
            }
        });
        
        
        
        private void updateTheData(String id, String name, String message) {

        db.collection("Messages").document(id).update("Name", name, "Message", message)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Data Updated", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, "Something went wrong ::: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveToFireStore(String id, String userName, String userMessage) {

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("Name", userName);
        data.put("Message", userMessage);

        db.collection("Messages").document(id).set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Data Saved to DB", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
```

#### ShowMessage.java
```
RecyclerView recyclerView;
CustomAdapter adapter;
List<Model> list;

FirebaseFirestore db;

        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        list = new ArrayList<>();
        adapter = new CustomAdapter(list, ShowMessage.this);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        showData();
        
        
        
         public void showData() {
        db.collection("Messages").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        list.clear();

                        for (DocumentSnapshot snapshot : task.getResult()) {
                            Model model = new Model(snapshot.getString("id"), snapshot.getString("Name"), snapshot.getString("Message"));
                            list.add(model);
                        }

                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ShowMessage.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                        Toast.makeText(ShowMessage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
```

#### CustomAdapter.java
```
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    ShowMessage activity;
    List<Model> list;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CustomAdapter(List<Model> list, ShowMessage activity) {
        this.activity = activity;
        this.list = list;
    }

    public void updateData(int position) {
        Model model = list.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("id", model.getId());
        bundle.putString("name", model.getName());
        bundle.putString("message", model.getMessage());

        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    public void deleteData(int position) {
        Model model = list.get(position);

        db.collection("Messages").document(model.id).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            notifyRemoved(position);
                            Toast.makeText(activity, "Data Deleted", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void notifyRemoved(int position) {
        list.remove(position);
        notifyItemRemoved(position);
        activity.showData();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(activity).inflate(R.layout.message_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.name.setText(list.get(position).getName());
        holder.message.setText(list.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.textViewName);
            message = itemView.findViewById(R.id.textViewMessage);
        }
    }
}
```

#### TouchHelper.java
```
public class TouchHelper extends ItemTouchHelper.SimpleCallback {

    CustomAdapter adapter;

    public TouchHelper(CustomAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();

        if (direction == ItemTouchHelper.LEFT) {
            //update the data
            adapter.updateData(position);
            adapter.notifyDataSetChanged();

        } else {
            //delete the data
            adapter.deleteData(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeRightBackgroundColor(Color.RED)
                .addSwipeRightActionIcon(R.drawable.delete)
                .addSwipeLeftBackgroundColor(R.color.purple_500)
                .addSwipeRightActionIcon(R.drawable.edit)
                .create()
                .decorate();

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
```

#### Model.java
```
public class Model {

    String id;
    String name;
    String message;

    public Model(String id, String name, String message) {
        this.id = id;
        this.name = name;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Model{" +
                "name='" + name + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
```

# App Highlight

![CRUD_1_App](https://user-images.githubusercontent.com/74413402/192092356-cfa3fa46-fd9e-4d7c-a0d2-57ac2557f1c8.png)
![CRUD_2_App](https://user-images.githubusercontent.com/74413402/192092358-f7873c8a-8cee-430e-b7fd-a90bd9b942bf.png)
![CRUD_Code](https://user-images.githubusercontent.com/74413402/192092359-72952013-4b3a-4426-ac65-8d449407c535.png)

