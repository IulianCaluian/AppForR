import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;

import org.json.*;

import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;


public class ProgramCastig {
    private JTextPane tPane;
    private static int contList = 0;
    private static SimpleDateFormat formatData;
    private List<Track> mTracks;
    private static URL url;
    private static final String GOLDENLINK ="https://www.europafm.ro/track_info.json";
    javax.sound.midi.Track track=null;
    Sequencer sequencer=null;
    Sequence sequence=null;

    static {
        try{
            url = new URL(GOLDENLINK);
        }catch (Exception e){
            e.printStackTrace();
        }

        formatData = new SimpleDateFormat("HH:mm");
    }

    public static void main(String[] args) {
        ProgramCastig pca = new ProgramCastig();
        pca.go();
    }

    private void go(){

        mTracks = new ArrayList<>();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 500);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JPanel bottomPanel = new JPanel();

        EmptyBorder eb = new EmptyBorder(new Insets(10, 10, 10, 10));
        tPane = new JTextPane();
        tPane.setMargin(new Insets(5, 5, 5, 5));
        tPane.setBorder(eb);

        JScrollPane qScroller = new JScrollPane(tPane);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JButton sunetButton = new JButton("Fa un sunet!");
        sunetButton.addActionListener(new InterfataCareFaceSunet());
        bottomPanel.add(sunetButton);

        JButton opresteButton = new JButton("Opreste sunet!");
        opresteButton.addActionListener(new MyStopListener());
        bottomPanel.add(opresteButton);

        frame.getContentPane().add(BorderLayout.CENTER,qScroller);
        frame.getContentPane().add(BorderLayout.SOUTH,bottomPanel);
        frame.setSize(800,1000);
        frame.setVisible(true);

        setUPMidi();
    }


    private void appendToPane(JTextPane tp, String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }

    TimerTask timerTask = new TimerTask()
    {
        public void run()
        {
            String str = null;
            try {
                Scanner scan = new Scanner(url.openStream());

                str = new String();
                while (scan.hasNext())
                    str += scan.nextLine();
                scan.close();
            }catch (Exception e){
              //  e.printStackTrace();
            }

            try{
                JSONObject obj = new JSONObject(str);
                JSONArray songs = obj.getJSONArray("songs");
                    int i=0;
                    String artist = songs.getJSONObject(i).getString("artist");
                    String piesa = songs.getJSONObject(i).getString("track");
                    Track t = new Track(piesa,artist);
                    String numeCurent = null;
                   if(mTracks.size()>0) {
                       Track curent = mTracks.get(mTracks.size() - 1);
                       numeCurent = curent.getName() + t.getArtist();
                   }
                   String numeT = t.getName()+t.getArtist();
                   int hour = t.getData().getHours();
                    if( ! numeT.equals(numeCurent) && hour >= 9) {
                        updateAdd(t);
                    }
            } catch(Exception e){
           //    e.printStackTrace();
            }
        }
    };

    private void updateAdd(Track t){


        //Piesa anterioara:
        Date rep = null;
        String curent = t.getName()+t.getArtist();

        if(curent.toLowerCase().contains("loredana")) {
            produSunetAlerta();
        }

        for (int i= mTracks.size() -1; i>=0;  i--){
            Track p = mTracks.get(i);
           // System.out.println(p.getName() + "-" + p.getArtist() + "-" + t.getData());
            String repet = p.getName()+p.getArtist();
            if(curent.equals(repet)){
                rep = p.getData();
                break;
            }
        }

        mTracks.add(t);
        rep = null;

        String data = formatData.format(t.getData());

        Color green = new Color(50,205,50);
        Color red = new Color(230,10,10);

        String str  = data + " - "+ t.getName() + "  -  " + t.getArtist()   +"\t-\t";
        appendToPane(tPane, str, Color.black);

        if(rep == null)
        appendToPane(tPane, "Fara repetitie"+'\n', green);
        else {
            appendToPane(tPane, "REPETATA!!! - " + formatData.format(rep)+'\n', red);
             produSunetAlerta();
        }
    }

    private void setUPMidi(){
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ,4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);

        } catch(Exception e) {e.printStackTrace();}
    }

    private void produSunetAlerta(){

        sequence.deleteTrack(track);
        track = sequence.createTrack();
        setUPMidi();


        for(int i=0; i<16; i+=2) {
            track.add(makeEvent(144, 9, 56, 80, 2*i));
            track.add(makeEvent(128, 9, 56, 80, 2*i + 2));
        }

        for(int i=0; i<16; i+=2) {
            track.add(makeEvent(144, 9, 46, 80, 2*i+1));
            track.add(makeEvent(128, 9, 46, 80, 2*i + 3));
        }

        try {
            track.add(makeEvent(192,9,1,0,15));
            sequencer.setSequence(sequence);
            //sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch(Exception e) {e.printStackTrace();}

    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch(Exception e) {e.printStackTrace(); }
        return event;
    }

    private class InterfataCareFaceSunet implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            produSunetAlerta();
        }
    }

    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            sequencer.stop();
        }
    }

}